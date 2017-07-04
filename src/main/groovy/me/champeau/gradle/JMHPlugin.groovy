/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.gradle

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.EclipseWtpPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

import java.util.concurrent.atomic.AtomicReference

/**
 * Configures the JMH Plugin.
 */
class JMHPlugin implements Plugin<Project> {
    public static final String JMH_CORE_DEPENDENCY = 'org.openjdk.jmh:jmh-core:'
    public static final String JMH_GENERATOR_DEPENDENCY = 'org.openjdk.jmh:jmh-generator-bytecode:'
    public static final String JMH_GROUP = 'jmh'
    public static final String JMH_NAME = 'jmh'
    public static final String JMH_JAR_TASK_NAME = 'jmhJar'
    public static final String JMH_TASK_COMPILE_GENERATED_CLASSES_NAME = 'jmhCompileGeneratedClasses'

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        final JMHPluginExtension extension = project.extensions.create(JMH_NAME, JMHPluginExtension, project)
        final Configuration configuration = project.configurations.create(JMH_NAME)
        final Configuration runtimeConfiguration = createJmhRuntimeConfiguration(project, extension)
        configuration.incoming.beforeResolve { ResolvableDependencies resolvableDependencies ->
            DependencyHandler dependencyHandler = project.getDependencies();
            def dependencies = configuration.getDependencies()
            dependencies.add(dependencyHandler.create("${JMH_CORE_DEPENDENCY}${extension.jmhVersion}"))
            dependencies.add(dependencyHandler.create("${JMH_GENERATOR_DEPENDENCY}${extension.jmhVersion}"))
        }

        ensureTasksNotExecutedConcurrently(project)

        def hasShadow = project.plugins.findPlugin('com.github.johnrengelman.shadow') != null

        createJmhSourceSet(project)

        registerBuildListener(project, extension)

        def jmhGeneratedSourcesDir = project.file("$project.buildDir/jmh-generated-sources")
        def jmhGeneratedClassesDir = project.file("$project.buildDir/jmh-generated-classes")
        createJmhRunBytecodeGeneratorTask(project, jmhGeneratedSourcesDir, extension, jmhGeneratedClassesDir)

        createJmhCompileGeneratedClassesTask(project, jmhGeneratedSourcesDir, jmhGeneratedClassesDir, extension)

        def metaInfExcludes = ['META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA']
        if (hasShadow) {
            createShadowJmhJar(project, extension, jmhGeneratedClassesDir, metaInfExcludes, runtimeConfiguration)
        } else {
            createStandardJmhJar(project, extension, metaInfExcludes, jmhGeneratedClassesDir, runtimeConfiguration)
        }

        project.tasks.create(name: JMH_NAME, type: JMHTask) {
            group JMH_GROUP
            dependsOn project.jmhJar
        }

        configureIDESupport(project)
    }

    private configureIDESupport(Project project) {
        project.afterEvaluate {
            def hasIdea = project.plugins.findPlugin(IdeaPlugin)
            if (hasIdea) {
                project.idea {
                    module {
                        scopes.TEST.plus += [project.configurations.jmh]
                    }
                }
                project.idea {
                    module {
                        project.sourceSets.jmh.java.srcDirs.each {
                            testSourceDirs += project.file(it)
                        }
                    }
                }
            }
            def hasEclipsePlugin = project.plugins.findPlugin(EclipsePlugin)
            def hasEclipseWtpPlugin = project.plugins.findPlugin(EclipseWtpPlugin)
            if (hasEclipsePlugin != null || hasEclipseWtpPlugin != null) {
                project.eclipse {
                    classpath.plusConfigurations += [project.configurations.jmh]
                }
            }
        }
    }

    @CompileStatic
    private static ensureTasksNotExecutedConcurrently(Project project) {
        def rootExtra = project
                .rootProject
                .extensions
                .extraProperties
        AtomicReference<JMHTask> lastAddedRef = rootExtra.has('jmhLastAddedTask') ?
                (AtomicReference<JMHTask>)rootExtra.get('jmhLastAddedTask') : new AtomicReference<>()
        rootExtra.set('jmhLastAddedTask', lastAddedRef)

        project.tasks.whenTaskAdded(new Action<Task>() {
            @Override
            void execute(final Task task) {
                if (task instanceof JMHTask) {
                    def lastAdded = lastAddedRef.getAndSet(task)
                    if (lastAdded) {
                        task.mustRunAfter(lastAdded)
                    }
                }
            }
        })
    }

    private Task createJmhCompileGeneratedClassesTask(Project project, File jmhGeneratedSourcesDir, File jmhGeneratedClassesDir, JMHPluginExtension extension) {
        project.tasks.create(name: JMH_TASK_COMPILE_GENERATED_CLASSES_NAME, type: JavaCompile) {
            group JMH_GROUP
            dependsOn 'jmhRunBytecodeGenerator'
            inputs.dir jmhGeneratedSourcesDir
            outputs.dir jmhGeneratedClassesDir

            classpath = project.sourceSets.jmh.runtimeClasspath
            if (extension.includeTests) {
                classpath += project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
            }
            source = project.fileTree(jmhGeneratedSourcesDir)
            destinationDir = jmhGeneratedClassesDir
        }
    }

    private Task createJmhRunBytecodeGeneratorTask(Project project, File jmhGeneratedSourcesDir, JMHPluginExtension extension, File jmhGeneratedClassesDir) {
        project.tasks.create(name: 'jmhRunBytecodeGenerator', type: JmhBytecodeGeneratorTask) {
            group JMH_GROUP
            dependsOn 'jmhClasses'
            includeTests = extension.includeTestsProvider
            generatedClassesDir = jmhGeneratedClassesDir
            generatedSourcesDir = jmhGeneratedSourcesDir
        }
    }

    private void createJmhSourceSet(Project project) {
        project.sourceSets {
            jmh {
                java.srcDir 'src/jmh/java'
                resources.srcDir 'src/jmh/resources'
                compileClasspath += project.configurations.jmh + project.configurations.compile + main.output
                runtimeClasspath += project.configurations.jmh + project.configurations.runtime + main.output
            }
        }
    }

    private void createShadowJmhJar(Project project, JMHPluginExtension extension, File jmhGeneratedClassesDir, List<String> metaInfExcludes, Configuration runtimeConfiguration) {
        def shadow = project.tasks.create(name: JMH_JAR_TASK_NAME, type: Class.forName('com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar', true, JMHPlugin.classLoader))
        shadow.group = JMH_GROUP
        shadow.dependsOn(JMH_TASK_COMPILE_GENERATED_CLASSES_NAME)
        shadow.description = 'Create a combined JAR of project and runtime dependencies'
        shadow.conventionMapping.with {
            map('classifier') {
                JMH_NAME
            }
        }
        shadow.manifest.inheritFrom project.tasks.jar.manifest
        shadow.manifest.attributes 'Main-Class': 'org.openjdk.jmh.Main'
        shadow.from(runtimeConfiguration)
        shadow.doFirst { task ->
            def processLibs = { files ->
                if (files) {
                    def libs = [task.manifest.attributes.get('Class-Path')]
                    libs.addAll files.collect { it.name }
                    task.manifest.attributes 'Class-Path': libs.unique().join(' ')
                }
            }
            processLibs project.configurations.jmh.files
            processLibs project.configurations.shadow.files

            if (extension.includeTests) {
                task.from(project.sourceSets.test.output)
            }
            task.eachFile { FileCopyDetails f ->
                if (f.name.endsWith('.class')) {
                    f.setDuplicatesStrategy(extension.duplicateClassesStrategy)
                }
            }
        }
        shadow.from(project.sourceSets.jmh.output)
        shadow.from(project.sourceSets.main.output)
        shadow.from(project.file(jmhGeneratedClassesDir))

        shadow.exclude(metaInfExcludes)
        shadow.configurations = []
    }

    private Task createStandardJmhJar(Project project, JMHPluginExtension extension, List<String> metaInfExcludes, File jmhGeneratedClassesDir, Configuration runtimeConfiguration) {
        project.tasks.create(name: JMH_JAR_TASK_NAME, type: Jar) {
            group JMH_GROUP
            dependsOn JMH_TASK_COMPILE_GENERATED_CLASSES_NAME
            inputs.dir project.sourceSets.jmh.output
            from(runtimeConfiguration) {
                exclude metaInfExcludes
            }
            doFirst {
                from(project.sourceSets.jmh.output)
                from(project.sourceSets.main.output)
                from(project.file(jmhGeneratedClassesDir))
                if (extension.includeTests) {
                    from(project.sourceSets.test.output)
                }
                eachFile { FileCopyDetails f ->
                    if (f.name.endsWith('.class')) {
                        f.setDuplicatesStrategy(extension.duplicateClassesStrategy)
                    }
                }
            }

            manifest {
                attributes 'Main-Class': 'org.openjdk.jmh.Main'
            }

            classifier = JMH_NAME
        }
    }

    private void registerBuildListener(
            final Project project, final JMHPluginExtension extension) {
        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                if (extension.includeTests) {
                    project.sourceSets {
                        jmh {
                            compileClasspath += test.output + project.configurations.testCompile
                            runtimeClasspath += test.output + project.configurations.testRuntime
                        }
                    }
                }

                def task = project.tasks.findByName(JMH_JAR_TASK_NAME)
                task.zip64 = extension.zip64
            }
        })
    }

    @CompileStatic
    private static Configuration createJmhRuntimeConfiguration(Project project, JMHPluginExtension extension) {
        def newConfig = project.configurations.detachedConfiguration().setVisible(false)
        newConfig.dependencies.addAll(project.configurations.getByName('jmh').allDependencies)
        newConfig.dependencies.addAll(project.configurations.getByName('runtime').allDependencies)
        project.afterEvaluate {
            if (extension.includeTests) {
                newConfig.dependencies.addAll(project.configurations.getByName('testRuntime').allDependencies)
            }
        }
        newConfig
    }
}

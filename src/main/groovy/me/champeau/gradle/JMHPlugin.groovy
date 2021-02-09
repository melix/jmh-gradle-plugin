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
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.EclipseWtpPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.util.GradleVersion

import java.util.concurrent.atomic.AtomicReference

/**
 * Configures the JMH Plugin.
 */
class JMHPlugin implements Plugin<Project> {
    private static boolean IS_GRADLE_MIN_55 = GradleVersion.current() >= GradleVersion.version('5.5')

    public static final String JMH_CORE_DEPENDENCY = 'org.openjdk.jmh:jmh-core:'
    public static final String JMH_GENERATOR_DEPENDENCY = 'org.openjdk.jmh:jmh-generator-bytecode:'
    public static final String JMH_GROUP = 'jmh'
    public static final String JMH_NAME = 'jmh'
    public static final String JMH_JAR_TASK_NAME = 'jmhJar'
    public static final String JMH_TASK_COMPILE_GENERATED_CLASSES_NAME = 'jmhCompileGeneratedClasses'
    public static final String JHM_RUNTIME_CONFIGURATION = 'jmhRuntimeClasspath'

    void apply(Project project) {
        if (!IS_GRADLE_MIN_55) {
            throw new RuntimeException("This version of the JMH Gradle plugin requires Gradle 5.5+, you are using ${GradleVersion.current().version}. Please upgrade Gradle or use an older version of the plugin.");
        }
        project.plugins.apply(JavaPlugin)
        final JMHPluginExtension extension = project.extensions.create(JMH_NAME, JMHPluginExtension, project)
        final Configuration configuration = project.configurations.create(JMH_NAME)
        final Configuration runtimeConfiguration = createJmhRuntimeConfiguration(project, extension)

        DependencyHandler dependencyHandler = project.getDependencies()
        configuration.withDependencies {dependencies ->
            dependencies.add(dependencyHandler.create("${JMH_CORE_DEPENDENCY}${extension.jmhVersion}"))
            dependencies.add(dependencyHandler.create("${JMH_GENERATOR_DEPENDENCY}${extension.jmhVersion}"))
        }

        ensureTasksNotExecutedConcurrently(project)

        def hasShadow = project.plugins.findPlugin('com.github.johnrengelman.shadow') != null

        createJmhSourceSet(project)

        registerBuildListener(project, extension)

        def jmhGeneratedSourcesDir = project.file("$project.buildDir/jmh-generated-sources")
        def jmhGeneratedClassesDir = project.file("$project.buildDir/jmh-generated-classes")
        def jmhGeneratedResourcesDir = project.file("$project.buildDir/jmh-generated-resources")
        createJmhRunBytecodeGeneratorTask(project, jmhGeneratedSourcesDir, extension, jmhGeneratedResourcesDir)

        createJmhCompileGeneratedClassesTask(project, jmhGeneratedSourcesDir, jmhGeneratedClassesDir, extension)

        def metaInfExcludes = ['module-info.class', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA']
        if (hasShadow) {
            createShadowJmhJar(project, extension, jmhGeneratedResourcesDir, jmhGeneratedClassesDir, metaInfExcludes, runtimeConfiguration)
        } else {
            createStandardJmhJar(project, extension, metaInfExcludes, jmhGeneratedResourcesDir, jmhGeneratedClassesDir, runtimeConfiguration)
        }

        createTask(project, JMH_NAME, JMHTask) {
            it.group JMH_GROUP
            it.dependsOn project.jmhJar
            it.jarArchive.set(project.jmhJar.archiveFile)
            it.benchmarkList = new File(jmhGeneratedResourcesDir, "META-INF/BenchmarkList")
            it.compilerHints = new File(jmhGeneratedResourcesDir, "/META-INF/CompilerHints")
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
                (AtomicReference<JMHTask>) rootExtra.get('jmhLastAddedTask') : new AtomicReference<JMHTask>()
        rootExtra.set('jmhLastAddedTask', lastAddedRef)

        project.tasks.withType(JMHTask).configureEach(new Action<JMHTask>() {
            @Override
            void execute(final JMHTask task) {
                def lastAdded = lastAddedRef.getAndSet(task)
                if (lastAdded) {
                    task.mustRunAfter(lastAdded)
                }
            }
        })
    }

    private Task createJmhCompileGeneratedClassesTask(Project project, File jmhGeneratedSourcesDir, File jmhGeneratedClassesDir, JMHPluginExtension extension) {
        createTask(project, JMH_TASK_COMPILE_GENERATED_CLASSES_NAME, JavaCompile) {
            it.group JMH_GROUP
            it.dependsOn 'jmhRunBytecodeGenerator'

            it.classpath = project.sourceSets.jmh.runtimeClasspath
            if (extension.includeTests) {
                it.classpath += project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
            }
            it.source = jmhGeneratedSourcesDir
            it.destinationDir = jmhGeneratedClassesDir
        }
    }

    private Task createJmhRunBytecodeGeneratorTask(Project project, File jmhGeneratedSourcesDir, JMHPluginExtension extension, File jmhGeneratedResourcesDir) {
        createTask(project, 'jmhRunBytecodeGenerator', JmhBytecodeGeneratorTask) {
            it.group JMH_GROUP
            it.dependsOn 'jmhClasses'
            it.jmhClasspath = project.configurations.jmh
            it.includeTests = extension.includeTestsProvider
            it.generatedClassesDir = jmhGeneratedResourcesDir
            it.generatedSourcesDir = jmhGeneratedSourcesDir
        }
    }

    private void createJmhSourceSet(Project project) {
        project.sourceSets {
            jmh {
                java.srcDir 'src/jmh/java'
                resources.srcDir 'src/jmh/resources'
                compileClasspath += main.output
                runtimeClasspath += main.output
            }
        }
        project.configurations.with {
            // the following line is for backwards compatibility
            // no one should really add directly to the "jmh" configuration
            jmhImplementation.extendsFrom(jmh)

            jmhCompileClasspath.extendsFrom(implementation, compileOnly)
            jmhRuntimeClasspath.extendsFrom(implementation, runtimeOnly)
        }
    }

    private void createShadowJmhJar(Project project, JMHPluginExtension extension, File jmhGeneratedResourcesDir, File jmhGeneratedClassesDir, List<String> metaInfExcludes, FileCollection runtimeConfiguration) {
        createTask(project, JMH_JAR_TASK_NAME, Class.forName('com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar', true, JMHPlugin.classLoader)) {
            it.group = JMH_GROUP
            it.dependsOn(JMH_TASK_COMPILE_GENERATED_CLASSES_NAME)
            it.description = 'Create a combined JAR of project and runtime dependencies'
            it.conventionMapping.with {
                map('classifier') {
                    JMH_NAME
                }
            }
            it.manifest.inheritFrom project.tasks.jar.manifest
            it.manifest.attributes 'Main-Class': 'org.openjdk.jmh.Main'
            it.from(runtimeConfiguration)
            FileCollection shadowConfiguration = project.configurations.shadow
            def testSourceSetOutput = project.sourceSets.test.output
            it.doFirst { task ->
                def processLibs = { files ->
                    if (files) {
                        def libs = [task.manifest.attributes.get('Class-Path')]
                        libs.addAll files.collect { it.name }
                        task.manifest.attributes 'Class-Path': libs.unique().join(' ')
                    }
                }
                processLibs runtimeConfiguration.files
                processLibs shadowConfiguration.files

                if (extension.includeTests) {
                    task.from(testSourceSetOutput)
                }
                task.eachFile { FileCopyDetails f ->
                    if (f.name.endsWith('.class')) {
                        f.setDuplicatesStrategy(extension.duplicateClassesStrategy)
                    }
                }
            }
            it.from(project.sourceSets.jmh.output)
            it.from(project.sourceSets.main.output)
            it.from(project.file(jmhGeneratedClassesDir))
            it.from(project.file(jmhGeneratedResourcesDir))

            it.exclude(metaInfExcludes)
            it.configurations = []
        }
    }

    private Task createStandardJmhJar(Project project, JMHPluginExtension extension, List<String> metaInfExcludes, File jmhGeneratedResourcesDir, File jmhGeneratedClassesDir, Configuration runtimeConfiguration) {
        createTask(project, JMH_JAR_TASK_NAME, Jar) {
            it.group JMH_GROUP
            it.dependsOn JMH_TASK_COMPILE_GENERATED_CLASSES_NAME
            it.inputs.files project.sourceSets.jmh.output
            it.inputs.files project.sourceSets.main.output
            it.duplicatesStrategy = extension.duplicateClassesStrategy
            if (extension.includeTests) {
                it.inputs.files project.sourceSets.test.output
            }
            it.from {
                runtimeConfiguration.asFileTree.collect { File f ->
                    f.isDirectory() ? f : project.zipTree(f)
                }
            }.exclude(metaInfExcludes)
            def jmhSourceSetOutput = project.sourceSets.jmh.output
            def mainSourceSetOutput = project.sourceSets.main.output
            def testSourceSetOutput = project.sourceSets.test.output

            it.from(jmhSourceSetOutput)
            it.from(mainSourceSetOutput)
            it.from(jmhGeneratedClassesDir)
            it.from(jmhGeneratedResourcesDir)
            if (extension.includeTests) {
                it.from(testSourceSetOutput)
            }

            it.manifest {
                attributes 'Main-Class': 'org.openjdk.jmh.Main'
            }

            it.archiveClassifier = JMH_NAME
        }
    }

    private void registerBuildListener(
            final Project project, final JMHPluginExtension extension) {
        project.gradle.projectsEvaluated {
            if (extension.includeTests) {
                project.sourceSets {
                    jmh {
                        compileClasspath += test.output + project.configurations.testCompileClasspath
                        runtimeClasspath += test.output + project.configurations.testRuntimeClasspath
                    }
                }
            }

            def task = project.tasks.findByName(JMH_JAR_TASK_NAME)
            task.zip64 = extension.zip64
        }
    }

    // TODO: This is really bad. We shouldn't use "runtime", but use the configurations provided by Gradle
    // automatically when creating a source set. That is to say, "jmhRuntimeOnly" for example and wire
    // our classpath properly
    @CompileStatic
    private static Configuration createJmhRuntimeConfiguration(Project project, JMHPluginExtension extension) {
        def newConfig = project.configurations.create(JHM_RUNTIME_CONFIGURATION)
        newConfig.setCanBeConsumed(false)
        newConfig.setCanBeResolved(true)
        newConfig.setVisible(false)
        newConfig.extendsFrom(project.configurations.getByName('jmh'))
        newConfig.extendsFrom(project.configurations.getByName('runtimeClasspath'))
        project.afterEvaluate {
            if (extension.includeTests) {
                newConfig.extendsFrom(project.configurations.getByName('testRuntimeClasspath'))
            }
        }
        newConfig
    }

    @CompileStatic
    private static <T extends Task> void createTask(Project project, String name, Class<T> type, Action<? super T> configuration) {
        project.tasks.register(name, type, configuration)
    }
}

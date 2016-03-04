/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.gradle

import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Configures the JMH Plugin.
 *
 * @author Cédric Champeau
 */
class JMHPlugin implements Plugin<Project> {
    static final String JMH_CORE_DEPENDENCY = 'org.openjdk.jmh:jmh-core:'
    static final String JMH_GENERATOR_DEPENDENCY = 'org.openjdk.jmh:jmh-generator-bytecode:'
    static final String JMH_GROUP = 'jmh'

    void apply(Project project) {
        final JMHPluginExtension extension = project.extensions.create('jmh', JMHPluginExtension, project)
        final Configuration configuration = project.configurations.create('jmh')
        configuration.incoming.beforeResolve { ResolvableDependencies resolvableDependencies ->
            DependencyHandler dependencyHandler = project.getDependencies();
            def dependencies = configuration.getDependencies()
            dependencies.add(dependencyHandler.create("${JMH_CORE_DEPENDENCY}${extension.jmhVersion}"))
            dependencies.add(dependencyHandler.create("${JMH_GENERATOR_DEPENDENCY}${extension.jmhVersion}"))
        }

        project.sourceSets {
            jmh {
                compileClasspath += project.configurations.jmh + project.configurations.compile + main.output
                runtimeClasspath += project.configurations.jmh + project.configurations.runtime + main.output
            }
        }

        registerBuildListener(project, extension)

        def jmhGeneratedSourcesDir = project.file("$project.buildDir/jmh-generated-sources")
        def jmhGeneratedClassesDir = project.file("$project.buildDir/jmh-generated-classes")
        project.tasks.create(name: 'jmhRunBytecodeGenerator', type: JavaExec) {
            group JMH_GROUP
            dependsOn 'jmhClasses'
            inputs.dir project.sourceSets.jmh.output
            outputs.dir jmhGeneratedSourcesDir

            main = 'org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator'
            classpath = project.sourceSets.jmh.runtimeClasspath
            if (extension.includeTests) {
                classpath += project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
            }
            args = [project.sourceSets.jmh.output.classesDir, jmhGeneratedSourcesDir, jmhGeneratedClassesDir, 'default']
        }

        project.tasks.create(name: 'jmhCompileGeneratedClasses', type: JavaCompile) {
            group JMH_GROUP
            dependsOn 'jmhRunBytecodeGenerator'
            inputs.dir jmhGeneratedSourcesDir
            outputs.dir jmhGeneratedClassesDir

            classpath = project.sourceSets.jmh.runtimeClasspath
            source = project.fileTree(jmhGeneratedSourcesDir)
            destinationDir = jmhGeneratedClassesDir
        }

        def metaInfExcludes = ['META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA']
        if (project.plugins.findPlugin('com.github.johnrengelman.shadow') == null) {
            project.tasks.create(name: 'jmhJar', type: Jar) {
                group JMH_GROUP
                dependsOn 'jmhCompileGeneratedClasses'
                inputs.dir project.sourceSets.jmh.output
                doFirst {
                    def filter = { it.isDirectory() ? it : project.zipTree(it) }
                    from(project.configurations.jmh.collect(filter)) {
                        exclude metaInfExcludes
                    }
                    from(project.configurations.runtime.collect(filter)) {
                        exclude metaInfExcludes
                    }
                    from(project.sourceSets.jmh.output)
                    from(project.sourceSets.main.output)
                    from(project.file(jmhGeneratedClassesDir))
                    if (extension.includeTests) {
                        from(project.configurations.testRuntime.collect(filter)) {
                            exclude metaInfExcludes
                        }
                        from(project.sourceSets.test.output)
                    }
                }

                manifest {
                    attributes 'Main-Class': 'org.openjdk.jmh.Main'
                }

                classifier = 'jmh'
                zip64 = { extension.zip64 }
            }
        } else {
            def shadow = project.tasks.create(name: 'jmhJar', type: Class.forName('com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar',true, JMHPlugin.classLoader))
            shadow.group = JMH_GROUP
            shadow.dependsOn('jmhCompileGeneratedClasses')
            shadow.description = 'Create a combined JAR of project and runtime dependencies'
            shadow.conventionMapping.with {
                map('classifier') {
                    'jmh'
                }
            }
            shadow.manifest.inheritFrom project.tasks.jar.manifest
            shadow.manifest.attributes 'Main-Class': 'org.openjdk.jmh.Main'
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
                    task.configurations += [project.configurations.testRuntime]
                    task.from(project.sourceSets.test.output)
                }
            }
            shadow.from(project.sourceSets.jmh.output)
            shadow.from(project.sourceSets.main.output)
            shadow.from(project.file(jmhGeneratedClassesDir))

            shadow.configurations = [project.configurations.runtime, project.configurations.jmh]
            shadow.exclude(metaInfExcludes)
        }

        project.tasks.create(name: 'jmh', type: JavaExec) {
            group JMH_GROUP
            dependsOn project.jmhJar
            main = 'org.openjdk.jmh.Main'
            classpath = project.files { project.jmhJar.archivePath }

            doFirst {
                args = [*args, *extension.buildArgs()]
                extension.humanOutputFile?.parentFile?.mkdirs()
                extension.resultsFile?.parentFile?.mkdirs()
            }
        }

        project.afterEvaluate {
            def hasIdea = project.plugins.findPlugin(org.gradle.plugins.ide.idea.IdeaPlugin)
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
            def hasEclipsePlugin = project.plugins.findPlugin(org.gradle.plugins.ide.eclipse.EclipsePlugin)
            def hasEclipseWtpPlugin = project.plugins.findPlugin(org.gradle.plugins.ide.eclipse.EclipseWtpPlugin)
            if (hasEclipsePlugin != null || hasEclipseWtpPlugin != null) {
                project.eclipse {
                    classpath.plusConfigurations += [ project.configurations.jmh ]
                }
            }
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
            }
        })
    }
}

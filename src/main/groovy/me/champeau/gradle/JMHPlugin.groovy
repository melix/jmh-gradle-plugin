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
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
/**
 * Configures the JMH Plugin.
 *
 * @author Cédric Champeau
 *
 */
class JMHPlugin implements Plugin<Project> {

    public static final String JMH_CORE_DEPENDENCY = 'org.openjdk.jmh:jmh-core:'
    public static final String JMH_ANNOT_PROC_DEPENDENCY = 'org.openjdk.jmh:jmh-generator-annprocess:'

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        final JMHPluginExtension extension = project.extensions.create('jmh', JMHPluginExtension, project)
        final Configuration configuration = project.configurations.create('jmh')

        project.sourceSets {
            jmh {
                java.srcDir 'src/jmh/java'
                if (project.plugins.hasPlugin('groovy')) {
                    groovy.srcDir 'src/jmh/groovy'
                }
                resources.srcDir 'src/jmh/resources'
                compileClasspath += project.configurations.jmh + project.configurations.compile + main.output
                runtimeClasspath += project.configurations.jmh + project.configurations.runtime + main.output
            }
        }

        configuration.getIncoming().beforeResolve(new Action<ResolvableDependencies>() {
            public void execute(ResolvableDependencies resolvableDependencies) {
                DependencyHandler dependencyHandler = project.getDependencies();
                def dependencies = configuration.getDependencies()
                dependencies.add(dependencyHandler.create(JMH_CORE_DEPENDENCY + extension.jmhVersion))
                dependencies.add(dependencyHandler.create(JMH_ANNOT_PROC_DEPENDENCY + extension.jmhVersion))
            }
        });

        if (project.plugins.findPlugin('com.github.johnrengelman.shadow') == null) {
            project.tasks.create(name: 'jmhJar', type: Jar) {
                dependsOn 'jmhClasses'
                inputs.dir project.sourceSets.jmh.output
                doFirst {
                    def filter = { it.isDirectory() ? it : project.zipTree(it) }
                    def exclusions = {
                        exclude '**/META-INF/services/**'
                        exclude '**/META-INF/*.SF'
                        exclude '**/META-INF/*.DSA'
                        exclude '**/META-INF/*.RSA'
                    }
                    from(project.configurations.jmh.collect(filter), exclusions)
                    from(project.configurations.compile.collect(filter), exclusions)
                    from(project.sourceSets.jmh.output)
                    from(project.sourceSets.main.output)
                    if (extension.includeTests) {
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

            shadow.group = 'jmh'
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
            }
            shadow.from(project.sourceSets.jmh.output)
            shadow.from(project.sourceSets.main.output)
            if (extension.includeTests) {
                shadow.from(project.sourceSets.test.output)
            }
            shadow.configurations = [project.configurations.runtime, project.configurations.jmh]
            shadow.exclude('META-INF/INDEX.LIST', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA')
        }

        project.tasks.create(name: 'jmh', type: JavaExec) {
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
        }

    }

}

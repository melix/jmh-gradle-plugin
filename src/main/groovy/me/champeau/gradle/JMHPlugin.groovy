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

import org.gradle.api.Plugin
import org.gradle.api.Project
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
    void apply(Project project) {
        project.plugins.apply(JavaPlugin)
        def extension = project.extensions.create('jmh', JMHPluginExtension, project)
        project.configurations.create('jmh')

        project.sourceSets {
            jmh {
                java.srcDir 'src/jmh/java'
                if (project.plugins.hasPlugin('groovy')) {
                    groovy.srcDir 'src/jmh/groovy'
                }
                resources.srcDir 'src/jmh/resources'
                compileClasspath += project.configurations.jmh + main.output
                runtimeClasspath += project.configurations.jmh + main.output
            }
        }
        project.dependencies {
            jmh 'org.openjdk.jmh:jmh-core:0.9.5'
            jmh 'org.openjdk.jmh:jmh-generator-annprocess:0.9.5'
            jmh 'net.sf.jopt-simple:jopt-simple:4.6'
            jmh 'org.apache.commons:commons-math3:3.2'
            jmh project.configurations.compile
        }

        project.tasks.create(name: 'jmhJar', type: Jar) {
            dependsOn 'jmhClasses'
            inputs.dir project.sourceSets.jmh.output
            doFirst {
                from(project.configurations.jmh.collect { it.isDirectory() ? it : project.zipTree(it) }) {
                    exclude '**/META-INF/services/**'
                    exclude '**/META-INF/*.SF'
                    exclude '**/META-INF/*.DSA'
                    exclude '**/META-INF/*.RSA'
                }
                from (project.sourceSets.jmh.output)
                from (project.sourceSets.main.output)
            }

            manifest {
                attributes 'Main-Class': 'org.openjdk.jmh.Main'
            }

            classifier = 'jmh'
        }
        project.tasks.create(name: 'jmh', type: JavaExec) {
            dependsOn project.jmhJar
            main = 'org.openjdk.jmh.Main'
            classpath = project.files(project.jmhJar.archivePath) + project.sourceSets.main.runtimeClasspath

            doFirst {
                args = [*args, *extension.buildArgs()]
                extension.humanOutputFile?.parentFile?.mkdirs()
                extension.resultsFile?.parentFile?.mkdirs()
            }
        }

    }

}

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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

class ProjectWithDuplicateClassesSpec extends Specification {
    File projectDir
    File buildFile
    List<File> pluginClasspath

    def setup() {
        projectDir = new File("src/funcTest/resources/java-project-with-duplicate-classes")
        buildFile = new File(projectDir, 'build.gradle')
        assert buildFile.createNewFile()
        def pluginClasspathResource = getClass().classLoader.getResourceAsStream("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def cleanup() {
        assert buildFile.delete()
    }

    def "Fail the build while executing jmhJar task"() {
        given:
        buildFile << """
            plugins {
                id 'java'
                id 'me.champeau.gradle.jmh'
            }

            repositories {
                jcenter()
            }
                    """
        BuildResult project = configure().buildAndFail()

        when:
        BuildTask taskResult = project.task(":jmhJar")

        then:
        taskResult.outcome == TaskOutcome.FAILED
    }

    GradleRunner configure() {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .withArguments("clean", "jmh")
    }

    def "Fail the build while executing jmhJar task when Shadow plugin applied"() {
        given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.github.johnrengelman.shadow'
                id 'me.champeau.gradle.jmh'
            }

            repositories {
                jcenter()
            }
                    """
        BuildResult project = configure().buildAndFail()

        when:
        BuildTask taskResult = project.task(":jmhJar")

        then:
        taskResult.outcome == TaskOutcome.FAILED
    }

    def "Show warning for duplicate classes when DuplicatesStrategy.WARN is used"() {
        given:
        buildFile << """
            plugins {
                id 'java'
                id 'me.champeau.gradle.jmh'
            }

            repositories {
                jcenter()
            }

            jmh {
                duplicateClassesStrategy = 'warn'
            }
                    """
        BuildResult project = configure().build()

        when:
        BuildTask taskResult = project.task(":jmh")

        then:
        taskResult.outcome == TaskOutcome.SUCCESS
        project.output.contains('"me/champeau/gradle/jmh/Helper.class"')
    }

    def "Show warning for duplicate classes when DuplicatesStrategy.WARN is used and Shadow plugin applied"() {
        given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.github.johnrengelman.shadow'
                id 'me.champeau.gradle.jmh'
            }

            repositories {
                jcenter()
            }

            jmh {
                duplicateClassesStrategy = 'warn'
            }
                    """
        BuildResult project = configure().build()

        when:
        BuildTask taskResult = project.task(":jmh")

        then:
        taskResult.outcome == TaskOutcome.SUCCESS
        project.output.contains('"me/champeau/gradle/jmh/Helper.class"')
    }
}

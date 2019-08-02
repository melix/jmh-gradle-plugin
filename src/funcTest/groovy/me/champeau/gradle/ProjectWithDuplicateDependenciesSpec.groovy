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

class ProjectWithDuplicateDependenciesSpec extends Specification {
    File projectDir
    File buildFile
    List<File> pluginClasspath

    def setup() {
        projectDir = new File("src/funcTest/resources/java-project-with-duplicate-dependencies")
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

    def "Run project with duplicate dependencies"() {
        given:
        createBuildFile("""
            plugins {
                id 'java'
                id 'me.champeau.gradle.jmh'
            }
        """)
        BuildResult project = build()

        when:
        BuildTask taskResult = project.task(":jmh");
        String benchmarkResults = new File(projectDir, "build/reports/benchmarks.csv").text

        then:
        taskResult.outcome == TaskOutcome.SUCCESS
        benchmarkResults.contains('JavaBenchmark.sqrtBenchmark')
    }

    BuildResult build() {
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .withArguments("clean", "jmh")
                .build()
    }

    void createBuildFile(String plugins) {
        buildFile << plugins
        buildFile << """

        repositories {
            jcenter()
        }

        dependencies {
            implementation 'org.apache.commons:commons-lang3:3.0.1'
            testImplementation 'junit:junit:4.12'
            testImplementation 'org.apache.commons:commons-lang3:3.2'
            jmh 'org.apache.commons:commons-lang3:3.4'
        }

        jmh {
            resultFormat = 'csv'
            resultsFile = file('build/reports/benchmarks.csv')
        }
        """
    }

    def "Run project with duplicate dependencies with Shadow applied"() {
        given:
        createBuildFile("""
            plugins {
                id 'java'
                id 'com.github.johnrengelman.shadow'
                id 'me.champeau.gradle.jmh'
            }
        """)
        BuildResult project = build()

        when:
        BuildTask taskResult = project.task(":jmh");
        String benchmarkResults = new File(projectDir, "build/reports/benchmarks.csv").text

        then:
        taskResult.outcome == TaskOutcome.SUCCESS
        benchmarkResults.contains('JavaBenchmark.sqrtBenchmark')
    }

}

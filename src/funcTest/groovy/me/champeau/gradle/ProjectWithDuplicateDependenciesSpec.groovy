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

import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class ProjectWithDuplicateDependenciesSpec extends AbstractFuncSpec {

    def setup() {
        usingSample('java-project-with-duplicate-dependencies')
    }

    def "Run project with duplicate dependencies (Gradle #gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)

        and:
        createBuildFile("""
            plugins {
                id 'java'
                id 'me.champeau.gradle.jmh'
            }
        """)

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        benchmarksCsv.text.contains('JavaBenchmark.sqrtBenchmark')

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }

    def "Run project with duplicate dependencies with Shadow applied (Gradle #gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)

        and:
        createBuildFile("""
            plugins {
                id 'java'
                id 'com.github.johnrengelman.shadow'
                id 'me.champeau.gradle.jmh'
            }
        """)

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        benchmarksCsv.text.contains('JavaBenchmark.sqrtBenchmark')

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
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
}

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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ProjectWithDuplicateDependenciesSpec extends AbstractFuncSpec {

    def setup() {
        usingSample('java-project-with-duplicate-dependencies')
    }

    def "Run project with duplicate dependencies"() {

        given:
        createBuildFile("""
            plugins {
                id 'java'
                id 'me.champeau.gradle.jmh'
            }
        """)

        when:
        def result = build("clean", "jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        file("build/reports/benchmarks.csv").text.contains('JavaBenchmark.sqrtBenchmark')
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

        when:
        def result = build("clean", "jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        file("build/reports/benchmarks.csv").text.contains('JavaBenchmark.sqrtBenchmark')
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

/*
 * Copyright 2014-2021 the original author or authors.
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
package me.champeau.jmh


import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class ToolchainCompatibilitySpec extends AbstractFuncSpec {

    def setup() {
        usingSample("java-project")
    }

    def "Executes benchmarks with expected toolchain (#gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)
        buildFile << """
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(11)
                }
            }
            
            tasks.named("jmh") {
                doFirst {
                    println "Using toolchain: \${javaLauncher.get().executablePath}"
                }
            }
        """

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }
}

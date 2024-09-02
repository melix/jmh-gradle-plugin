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

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.Unroll

@Unroll
class ProjectWithFeaturePreviewSpec extends AbstractFuncSpec {

    def setup() {
        usingSample('java-project-with-feature-previews')
    }

    def "successfully executes benchmark which uses feature previews (#gradleVersion)"() {
        given:
        usingGradleVersion(gradleVersion)

        and:
        // TODO: we can move this into the test fixture project once we drop support for Gradle 7.x
        if (gradleVersion >= GradleVersion.version("8.9")) {
            settingsFile.text =
            """
                plugins {
                    id("org.gradle.toolchains.foojay-resolver-convention")
                }\n
            """ + settingsFile.text
        }

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }
}

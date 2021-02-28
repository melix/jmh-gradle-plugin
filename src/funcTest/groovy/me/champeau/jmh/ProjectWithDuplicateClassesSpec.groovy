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

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class ProjectWithDuplicateClassesSpec extends AbstractFuncSpec {

    def setup() {
        usingSample('java-project-with-duplicate-classes')
    }

    def "Fail the build while executing jmhJar task (#gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)

        and:
        buildFile << """
            plugins {
                id 'java'
                id 'me.champeau.jmh'
            }

            repositories {
                mavenCentral()
            }
            
            jmh {
                duplicateClassesStrategy = DuplicatesStrategy.FAIL
            }
        """

        when:
        def result = buildAndFail("jmh")

        then:
        result.task(":jmhJar").outcome == FAILED

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }

    def "Fail the build while executing jmhJar task when Shadow plugin applied (#gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)
        withoutConfigurationCache('shadow plugin unsupported')

        and:
        buildFile << """
            plugins {
                id 'java'
                id 'com.github.johnrengelman.shadow'
                id 'me.champeau.jmh'
            }

            repositories {
                mavenCentral()
            }
            
            jmh {
                duplicateClassesStrategy = DuplicatesStrategy.FAIL
            }
        """

        when:
        def result = buildAndFail("jmh")

        then:
        result.task(":jmhJar").outcome == FAILED

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }

    def "Show warning for duplicate classes when DuplicatesStrategy.WARN is used (#gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)

        and:
        buildFile << """
            plugins {
                id 'java'
                id 'me.champeau.jmh'
            }

            repositories {
                mavenCentral()
            }

            jmh {
                duplicateClassesStrategy = DuplicatesStrategy.WARN
            }
        """

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        result.output.contains('"me/champeau/jmh/Helper.class"')

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }

    def "Show warning for duplicate classes when DuplicatesStrategy.WARN is used and Shadow plugin applied (#gradleVersion)"() {

        given:
        usingGradleVersion(gradleVersion)
        withoutConfigurationCache('shadow plugin unsupported')

        and:
        buildFile << """
            plugins {
                id 'java'
                id 'com.github.johnrengelman.shadow'
                id 'me.champeau.jmh'
            }

            repositories {
                mavenCentral()
            }

            jmh {
                duplicateClassesStrategy = DuplicatesStrategy.WARN
            }
        """

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        result.output.contains('"me/champeau/jmh/Helper.class"')

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }
}

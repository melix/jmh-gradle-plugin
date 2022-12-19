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

import org.gradle.util.GradleVersion
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

@Unroll
class ParameterSpec extends AbstractFuncSpec {

    def "Executes benchmarks with parameters"() {

        given:
        usingSample("java-project")

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        !result.output.contains('parameter option not respected')
    }

    def "executes with configuration cache"() {
        given:
        usingGradleVersion(GradleVersion.version("7.6"))
        usingSample("java-project")

        when:
        def result = build("jmhJar", "--configuration-cache")

        then:
        result.task(":jmhJar").outcome == SUCCESS
        result.output.contains("Calculating task graph as no configuration cache is available for tasks: jmhJar")

        when:
        result = build("jmhJar", "--configuration-cache")

        then:
        result.task(":jmhJar").outcome == UP_TO_DATE
        result.output.contains("Configuration cache entry reused.")

    }
}

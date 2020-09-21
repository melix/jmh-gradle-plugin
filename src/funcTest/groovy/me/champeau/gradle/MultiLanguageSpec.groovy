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
class MultiLanguageSpec extends AbstractFuncSpec {

    def "Execute #language benchmarks"() {

        given:
        usingSample("${language.toLowerCase()}-project")

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        file("build/reports/benchmarks.csv").text.contains(language + 'Benchmark.sqrtBenchmark')

        where:
        language << ['Groovy', 'Java', 'Kotlin', 'Scala']
    }
}

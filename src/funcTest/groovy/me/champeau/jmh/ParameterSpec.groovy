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
        usingSample("java-project")

        when:
        def result = build("jmhJar")

        then:
        result.task(":jmhJar").outcome == SUCCESS
        result.output.contains("Calculating task graph as no configuration cache is available for tasks: jmhJar") ||
                result.output.contains("Calculating task graph as no cached configuration is available for tasks: jmhJar")

        when:
        result = build("jmhJar")

        then:
        result.task(":jmhJar").outcome == UP_TO_DATE
        result.output.contains("Configuration cache entry reused.")

    }

    def "--jmhArgs project property is tokenized into JMH arguments"() {
        given:
        usingSample("java-project")

        when:
        def result = build("jmh", "--jmhArgs=-t 4 -wi 5 -i 10", "--info")

        then:
        result.output.contains('Running JMH with arguments:')
        // the --jmhArgs string is split into individual JMH tokens
        result.output.contains('-t')
        result.output.contains('4')
        result.output.contains('-wi')
        result.output.contains('5')
        result.output.contains('-i')
        result.output.contains('10')
    }

    def "--jmhArgs adds non-overlapping flags to jmhOptions"() {
        given:
        usingSample("java-project")

        when:
        // jmhOptions in build.gradle is ['-tu', 'ms']; --jmhArgs uses non-overlapping flags
        def result = build("jmh", "--jmhArgs=-f 1 -wi 5", "--info")

        then:
        result.output.contains('Running JMH with arguments:')
        // --jmhArgs flags are present
        result.output.contains('-wi, 5')
        result.output.contains('-f, 1')
        // buildscript's jmhOptions also survive (no overlap, so no conflict)
        result.output.contains('-tu, ms')
    }

    def "--jmhArgs replaces matching flags from jmhOptions"() {
        given:
        usingSample("java-project")

        when:
        // jmhOptions in build.gradle is ['-tu', 'ms']; --jmhArgs also sets -tu ns
        def result = build("jmh", "--jmhArgs=-tu ns -f 1", "--info")

        then:
        result.output.contains('Running JMH with arguments:')
        // --jmhArgs -tu ns replaces buildscript -tu ms
        result.output.contains('-tu, ns')
        !result.output.contains('-tu, ms')
        // new flags from --jmhArgs are appended
        result.output.contains('-f, 1')
    }

    def "--jmhArgs overrides modeled property from build.gradle"() {
        given:
        usingSample("java-project")

        when:
        // benchmarkMode in build.gradle is ['thrpt','ss'] → args contain -bm thrpt,ss
        def result = build("jmh", "--jmhArgs=-bm ss", "--info")

        then:
        result.output.contains('Running JMH with arguments:')
        // --jmhArgs replaces the modeled -bm value
        result.output.contains('-bm, ss')
        !result.output.contains('-bm, thrpt,ss')
    }
}

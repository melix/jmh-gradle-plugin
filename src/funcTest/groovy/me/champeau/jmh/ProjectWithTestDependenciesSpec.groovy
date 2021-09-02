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
import spock.lang.Unroll

@Unroll
class ProjectWithTestDependenciesSpec extends AbstractFuncSpec {

    def "Run project with dependencies on test sources (#gradleVersion)"() {

        given:
        usingSample('java-project-with-test-dependencies')
        usingGradleVersion(gradleVersion)

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == TaskOutcome.SUCCESS
        benchmarksCsv.text.contains('JavaBenchmarkThatDependsOnTest.sqrtBenchmark')

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }

    def "Run project with dependencies on test sources and configure-on-demand (#gradleVersion)"() {

        given:
        usingSample('java-project-with-test-dependencies')
        usingGradleVersion(gradleVersion)

        when:
        def result = build("--configure-on-demand", "jmh")

        then:
        result.task(":compileTestJava").outcome == TaskOutcome.SUCCESS
        !result.output.contains("Task ':compileJmhJava' uses this output of task ':compileTestJava' without declaring an explicit or implicit dependency.")

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }
}

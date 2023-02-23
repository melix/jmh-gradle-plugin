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
class UnsupportedGradleVersionSpec extends AbstractFuncSpec {

    def "Run project with dependencies on test sources (#gradleVersion)"() {

        given:
        usingSample('java-project-with-test-dependencies')
        usingGradleVersion(gradleVersion)

        when:
        def result = buildAndFail("jmh")

        then:
	result.output.contains('Please upgrade Gradle or use an older version of the JMH Gradle plugin.')

        where:
        gradleVersion << GradleVersion.version('6.9')
    }
}

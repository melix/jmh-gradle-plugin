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

@Unroll
class MultiLanguageSpec extends AbstractFuncSpec {

    def "Execute #language benchmarks (#gradleVersion)"() {

        given:
        usingSample("${language.toLowerCase()}-project")
        usingGradleVersion(gradleVersion)
        if (language == 'Kotlin') withoutConfigurationCache('kotlin plugin unsupported')

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        benchmarksCsv.text.contains(language + 'Benchmark.sqrtBenchmark')

        where:
        [language, gradleVersion] << [
                ['Groovy', 'Java', 'Kotlin', 'Scala'],
                TESTED_GRADLE_VERSIONS
        ].combinations().findAll { lang, gradleVer ->
            // TODO: remove this condition when TESTED_GRADLE_VERSIONS gets updated to 7.6.3 or higher.
            // Kotlin 2.1.0 requires the minimum Gradle version 7.6.3, see https://kotlinlang.org/docs/gradle-configure-project.html#kotlin-gradle-plugin-data-in-a-project
            !(lang == 'Kotlin' && gradleVer < GradleVersion.version('7.6.3'))
        }
    }

    def "Executes benchmarks with multiple languages"() {

        given:
        usingSample("mixed-language-project")

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        benchmarksCsv.text.contains('JavaBenchmark.sqrtBenchmark')
        benchmarksCsv.text.contains('GroovyBenchmark.sqrtBenchmark')
    }
}

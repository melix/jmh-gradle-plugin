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
class JmhWithShadowPluginSpec extends AbstractFuncSpec {

    def "Run #language benchmarks that are packaged with Shadow plugin (#gradleVersion #language #shadowPlugin)"() {

        given:
        def projectRoot = usingSample("${language.toLowerCase()}-shadow-project")
        def rootBuildFile = new File(projectRoot, 'build.gradle')
        def buildFileContent = rootBuildFile.text.replace("shadowPlugin", shadowPlugin)
        rootBuildFile.text = buildFileContent

        usingGradleVersion(gradleVersion)
        disableConfigCacheForShadow(shadowPlugin)

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        benchmarksCsv.text.contains(language + 'Benchmark.sqrtBenchmark')

        where:
        [language, gradleVersion, shadowPlugin] << [
                ['Java', 'Scala'],
                TESTED_SHADOW_GRADLE_COMBINATIONS,
        ].combinations { lang, tuple ->
            def (plugin, gradle) = tuple
            [lang, gradle, plugin]
        }
    }
}

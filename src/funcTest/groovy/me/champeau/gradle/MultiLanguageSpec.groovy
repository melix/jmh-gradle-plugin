/*
 * Copyright 2014-2016 the original author or authors.
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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MultiLanguageSpec extends Specification {
    def "Execute #language benchmarks"() {
        given:
        File projectDir = new File("src/funcTest/resources/${language.toLowerCase()}-project")
        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        List<String> pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

        BuildResult project = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath(pluginClasspath)
            .withArguments('-S', "clean", "jmh")
            .build();

        when:
        BuildTask taskResult = project.task(":jmh");
        String benchmarkResults = new File(projectDir, "build/reports/benchmarks.csv").text

        then:
        taskResult.outcome == TaskOutcome.SUCCESS
        benchmarkResults.contains(language + 'Benchmark.sqrtBenchmark')

        where:
        language << ['Groovy', 'Java', 'Kotlin', 'Scala']
    }
}

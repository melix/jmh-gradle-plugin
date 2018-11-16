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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

class ProjectWithTestDependenciesSpec extends Specification {
    def "Run project with dependencies on test sources"() {
        given:
        File projectDir = new File("src/funcTest/resources/java-project-with-test-dependencies")
        def pluginClasspathResource = getClass().classLoader.getResourceAsStream("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        List<File> pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

        BuildResult project = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath(pluginClasspath)
            .withArguments("clean", "jmh")
            .build();

        when:
        BuildTask taskResult = project.task(":jmh");
        String benchmarkResults = new File(projectDir, "build/reports/benchmarks.csv").text

        then:
        taskResult.outcome == TaskOutcome.SUCCESS
        benchmarkResults.contains('JavaBenchmarkThatDependsOnTest.sqrtBenchmark')
    }
}

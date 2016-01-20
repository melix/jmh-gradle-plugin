/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.Before
import org.junit.Test

class MultiLanguageTest {
    private File projectDir = new File("src/test/resources/multi-language-project")
    private List<String> pluginClasspath

    @Before
    public void setUp() {
        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    @Test
    public void runJmh() {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .withArguments("clean", "jmh")
                .build();

        BuildTask taskResult = result.task(":jmh");
        assert taskResult.outcome == TaskOutcome.SUCCESS

        String benchmarkResults = new File(projectDir, "build/reports/benchmarks.csv").text
        ['JavaBenchmark', 'ScalaBenchmark', 'GroovyBenchmark', 'KotlinBenchmark'].each { bench ->
            assert benchmarkResults.contains(bench+'.sqrtBenchmark')
        }
    }

}

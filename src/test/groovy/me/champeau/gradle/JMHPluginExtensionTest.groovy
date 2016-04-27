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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class JMHPluginExtensionTest {
    Project project;
    JMHPluginExtension extension;

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        extension = new JMHPluginExtension(project)
    }

    @Test
    void noBenchmarkModes() {
        extension.setBenchmarkMode(null)

        def result = extension.buildArgs()
        def filtered = result.grep({ it.startsWith("-bm") })
        assert filtered.isEmpty() == true
    }

    @Test
    void emptyBenchmarkModes() {
        extension.setBenchmarkMode(Collections.emptyList())

        def result = extension.buildArgs()
        def filtered = result.grep({ it.startsWith("-bm") })
        assert filtered.isEmpty() == true
    }

    @Test
    void singleBenchmarkModes() {
        extension.setBenchmarkMode(Arrays.asList('abc'))

        def result = extension.buildArgs()
        int index = result.indexOf("-bm")
        assert index >= 0
        assert result.get(index + 1) == 'abc'
    }


    @Test
    void multipleBenchmarkModes() {
        extension.setBenchmarkMode(Arrays.asList('abc', 'xyz', 'WWW', 'abc', 'www'))

        def result = extension.buildArgs()
        int index = result.indexOf("-bm")
        assert index >= 0
        assert result.get(index + 1) == 'abc,xyz,WWW,www'
    }
}

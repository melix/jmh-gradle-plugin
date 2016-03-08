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

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Ignore
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JMHPluginTest {
    @Test
    void testPluginIsApplied() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.gradle.jmh'


        def task = project.tasks.findByName('jmh')
        assert task instanceof JavaExec

    }

    @Test
    void testPluginIsAppliedWithGroovy() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'groovy'
        project.apply plugin: 'me.champeau.gradle.jmh'


        def task = project.tasks.findByName('jmh')
        assert task instanceof JavaExec

    }

    @Test
    void testPluginIsAppliedWithZip64() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'groovy'
        project.apply plugin: 'me.champeau.gradle.jmh'

        project.jmh.zip64 = true

        def task = project.tasks.findByName('jmhJar')
        assert task instanceof Jar
        assert task.zip64

    }

    @Ignore
    @Test
    void testPluginWithAlternativeJmhVersion() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.gradle.jmh'

        def expectedVersion = 'my.expected.version-SNAPSHOT'
        project.jmh.jmhVersion = expectedVersion

        def config = project.project.configurations.getByName('jmh')

        def dependencies = config.dependencies

        assert dependencies.isEmpty();

        // mock-trigger beforeResolve() to avoid 'real' resolution of dependencies
        DependencyResolutionListener broadcast = config.getDependencyResolutionBroadcast()
        ResolvableDependencies incoming = config.getIncoming()
        broadcast.beforeResolve(incoming)

        def dependencyHandler = project.getDependencies();

        assert dependencies.contains(dependencyHandler.create(JMHPlugin.JMH_ANNOT_PROC_DEPENDENCY + expectedVersion))
        assert dependencies.contains(dependencyHandler.create(JMHPlugin.JMH_CORE_DEPENDENCY + expectedVersion))
    }

    @Test
    void testResultsFileShouldProvidedResultsFile() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.gradle.jmh'

        File resultsFile = new File('some/result/file.pdf')
        project.jmh.resultsFile = resultsFile

        List options = project.jmh.buildArgs()
        assert project.relativePath(resultsFile) in options
    }

    @Test
    void testResultsFileShouldUseResultFormatAsExtension() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.gradle.jmh'

        project.jmh.resultFormat = 'json'

        File expectedFile = project.file(String.valueOf(project.getBuildDir()) + "/reports/jmh/results.json")
        List options = project.jmh.buildArgs()
        assert project.relativePath(expectedFile) in options
    }

    @Test
    void testAllJmhTasksBelongToJmhGroup() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.gradle.jmh'

        project.tasks.find { it.name.startsWith('jmh') }.each {
            assert it.group == JMHPlugin.JMH_GROUP
        }
    }

    @Test
    void testPluginIsAppliedTogetherWithShadow() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'com.github.johnrengelman.shadow'
        project.apply plugin: 'me.champeau.gradle.jmh'

        def task = project.tasks.findByName('jmhJar')
        assert task instanceof ShadowJar
    }
}

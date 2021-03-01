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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GFileUtils
import org.gradle.util.GradleVersion
import spock.lang.Specification
import spock.lang.TempDir

abstract class AbstractFuncSpec extends Specification {

    protected static final List<GradleVersion> TESTED_GRADLE_VERSIONS = [
            GradleVersion.version('6.8'),
            GradleVersion.current()
    ]

    @TempDir
    File temporaryFolder

    private GradleVersion testedGradleVersion = GradleVersion.current()

    private String noConfigurationCacheReason

    protected void usingGradleVersion(GradleVersion gradleVersion) {
        testedGradleVersion = gradleVersion
    }

    protected void withoutConfigurationCache(String reason) {
        noConfigurationCacheReason = reason
    }

    File getProjectDir() {
        temporaryFolder
    }

    File getBuildFile() {
        file('build.gradle')
    }

    File getBenchmarksCsv() {
        file("build/reports/benchmarks.csv")
    }

    protected void usingSample(String name) {
        File sampleDir = new File("src/funcTest/resources/$name")
        GFileUtils.copyDirectory(sampleDir, projectDir)
    }

    protected File file(String path) {
        new File(projectDir, path)
    }

    private List<File> getPluginClasspath() {
        def pluginClasspathResource = getClass().classLoader.getResourceAsStream("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        pluginClasspathResource.readLines().collect { new File(it) }
    }

    private GradleRunner gradleRunner(List<String> arguments) {
        GradleRunner.create()
                .withGradleVersion(testedGradleVersion.version)
                .forwardOutput()
                .withPluginClasspath(pluginClasspath)
                .withProjectDir(projectDir)
                .withArguments(arguments)
    }

    protected BuildResult build(String... arguments) {
        gradleRunner(calculateArguments(arguments)).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        gradleRunner(calculateArguments(arguments)).buildAndFail()
    }

    private List<String> calculateArguments(String... arguments) {
        def gradleVersionWithConfigurationCache = testedGradleVersion >= GradleVersion.version('6.6')
        if (gradleVersionWithConfigurationCache && noConfigurationCacheReason) {
            println("Configuration cache disabled: $noConfigurationCacheReason")
        }
        (gradleVersionWithConfigurationCache && !noConfigurationCacheReason
                ? ['--stacktrace',
                   '--configuration-cache',
                   // need to say to "warn" because for some reason the system property 'spock.iKnowWhatImDoing.disableGroovyVersionCheck'
                   // is leaking to the process under test as being read
                   '--configuration-cache-problems', 'warn']
                : ['--stacktrace']) + (arguments as List)
    }
}

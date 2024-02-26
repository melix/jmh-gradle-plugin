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
            GradleVersion.version('7.0'),
            GradleVersion.version('8.0'),
            GradleVersion.current()
    ]

    protected static final List<String> TESTED_SHADOW_PLUGINS = [
            'com.github.johnrengelman.shadow',
            'io.github.goooler.shadow'
    ]

    protected static final Map<String, String> TESTED_SHADOW_PLUGIN_FOLDERS = [
            'com.github.johnrengelman.shadow':  'shadow',
            'io.github.goooler.shadow':         'forked-shadow'
    ]

    @TempDir
    File temporaryFolder

    private GradleVersion testedGradleVersion = GradleVersion.current()

    private String noConfigurationCacheReason

    protected void usingGradleVersion(GradleVersion gradleVersion) {
        testedGradleVersion = gradleVersion
    }

    // TODO: We can remove this and fully enable CC in tests once bump the Shadow version to 8.1.1+.
    // TODO: But Kotlin test still fails, it was suppressed in 1bab41646df6f47aea84ea3febeeec1c76cd2e79, need to investigate.
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

    private GradleRunner gradleRunner(List<String> arguments) {
        GradleRunner.create()
                .withGradleVersion(testedGradleVersion.version)
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments(arguments)
                .withTestKitDir(testKitDir)
    }

    protected BuildResult build(String... arguments) {
        gradleRunner(calculateArguments(arguments)).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        gradleRunner(calculateArguments(arguments)).buildAndFail()
    }

    private List<String> calculateArguments(String... arguments) {
        (!noConfigurationCacheReason
                ? ['--stacktrace',
                   '--configuration-cache']
                : ['--stacktrace']) + (arguments as List)
    }

    private static File getTestKitDir() {
        def gradleUserHome = System.getenv("GRADLE_USER_HOME")
        if (!gradleUserHome) {
            gradleUserHome = new File(System.getProperty("user.home"), ".gradle").absolutePath
        }
        return new File(gradleUserHome, "testkit")
    }
}

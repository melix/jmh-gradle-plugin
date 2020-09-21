package me.champeau.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GFileUtils
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AbstractFuncSpec extends Specification {

    protected static final List<GradleVersion> TESTED_GRADLE_VERSIONS = [
            GradleVersion.version('5.5'),
            GradleVersion.current()
    ]

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    private GradleVersion testedGradleVersion = GradleVersion.current()

    protected void usingGradleVersion(GradleVersion gradleVersion) {
        testedGradleVersion = gradleVersion
    }

    File getProjectDir() {
        temporaryFolder.root
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
        ['--stacktrace'] + (arguments as List)
    }
}

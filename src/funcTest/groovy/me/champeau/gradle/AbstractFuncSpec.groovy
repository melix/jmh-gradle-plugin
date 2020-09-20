package me.champeau.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GFileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AbstractFuncSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File getProjectDir() {
        temporaryFolder.root
    }

    File getBuildFile() {
        file('build.gradle')
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

    protected GradleRunner gradleRunner() {
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
    }

    protected BuildResult build(String... arguments) {
        gradleRunner().withArguments(arguments).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        gradleRunner().withArguments(arguments).buildAndFail()
    }
}

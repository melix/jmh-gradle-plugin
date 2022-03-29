package me.champeau.jmh

import org.gradle.util.GradleVersion
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class JmhWithEnvironmentSpec extends AbstractFuncSpec {
    def "Executes benchmarks with environment"() {

        given:
        usingSample("java-project-for-env-support")
        usingGradleVersion(gradleVersion)

        when:
        def result = build("jmh")

        then:
        result.task(":jmh").outcome == SUCCESS
        result.output.contains('got MY_ENV1=my_value1')
        result.output.contains('got MY_ENV2=my_value2')

        where:
        gradleVersion << TESTED_GRADLE_VERSIONS
    }
}

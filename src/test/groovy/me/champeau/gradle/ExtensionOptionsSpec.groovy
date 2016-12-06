package me.champeau.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.options.ProfilerConfig
import org.openjdk.jmh.runner.options.TimeValue
import org.openjdk.jmh.runner.options.VerboseMode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ExtensionOptionsSpec extends Specification {
    @Shared Project project = ProjectBuilder.builder().build()

    def "Verify option #optionMethod with #value as #result (Optional)"() {
        given:
        JMHPluginExtension extension = new JMHPluginExtension(project)
        ExtensionOptions options = new ExtensionOptions(extension)

        when:
        extension."$extensionMethod"(value)

        then:
        result == options."$optionMethod"().orElse(null)

        where:
        optionMethod               | extensionMethod            | value                   || result
        'getOutput'                | 'setHumanOutputFile'       | null                    || null
        'getOutput'                | 'setHumanOutputFile'       | project.file('foo.txt') || project.file('foo.txt').absolutePath
        'getResultFormat'          | 'setResultFormat'          | null                    || null
        'getResultFormat'          | 'setResultFormat'          | 'text'                  || ResultFormatType.TEXT
        'shouldDoGC'               | 'setForceGC'               | null                    || null
        'shouldDoGC'               | 'setForceGC'               | true                    || true
        'shouldDoGC'               | 'setForceGC'               | false                   || false
        'verbosity'                | 'setVerbosity'             | null                    || null
        'verbosity'                | 'setVerbosity'             | 'extra'                 || VerboseMode.EXTRA
        'shouldFailOnError'        | 'setFailOnError'           | null                    || null
        'shouldFailOnError'        | 'setFailOnError'           | true                    || true
        'shouldFailOnError'        | 'setFailOnError'           | false                   || false
        'getThreads'               | 'setThreads'               | null                    || null
        'getThreads'               | 'setThreads'               | 100                     || 100
        'getThreadGroups'          | 'setThreadGroups'          | null                    || null
        'getThreadGroups'          | 'setThreadGroups'          | [1, 2, 3]               || [1, 2, 3] as Integer[]
        'shouldSyncIterations'     | 'setSynchronizeIterations' | null                    || null
        'shouldSyncIterations'     | 'setSynchronizeIterations' | true                    || true
        'shouldSyncIterations'     | 'setSynchronizeIterations' | false                   || false
        'getWarmupIterations'      | 'setWarmupIterations'      | null                    || null
        'getWarmupIterations'      | 'setWarmupIterations'      | 100                     || 100
        'getWarmupTime'            | 'setWarmup'                | null                    || null
        'getWarmupTime'            | 'setWarmup'                | '1ns'                   || TimeValue.nanoseconds(1)
        'getMeasurementIterations' | 'setIterations'            | null                    || null
        'getMeasurementIterations' | 'setIterations'            | 100                     || 100
    }

    def "Verify option #optionMethod with #value as #result (direct)"() {
        given:
        JMHPluginExtension extension = new JMHPluginExtension(project)
        ExtensionOptions options = new ExtensionOptions(extension)

        when:
        extension."$extensionMethod"(value)

        then:
        result == options."$optionMethod"()

        where:
        optionMethod        | extensionMethod       | value              | result
        'getProfilers'      | 'setProfilers'        | null               | []
        'getProfilers'      | 'setProfilers'        | []                 | []
        'getProfilers'      | 'setProfilers'        | ['foo', 'bar:bar'] | [new ProfilerConfig('foo'), new ProfilerConfig('bar', 'bar')]
        'getWarmupIncludes' | 'setWarmupBenchmarks' | null               | []
        'getWarmupIncludes' | 'setWarmupBenchmarks' | ['a', 'b']         | ['a', 'b']
    }

    private static String capitalize(String str) {
        if (str == null || str.size() == 0) return str
        if (str.size() == 1) return str.toUpperCase()
        return str[0].toUpperCase() + str[1..-1]
    }
}

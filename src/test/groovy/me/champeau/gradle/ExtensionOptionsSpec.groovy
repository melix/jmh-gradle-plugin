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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.options.ProfilerConfig
import org.openjdk.jmh.runner.options.TimeValue
import org.openjdk.jmh.runner.options.VerboseMode
import org.openjdk.jmh.runner.options.WarmupMode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit;

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
        'getMeasurementBatchSize'     | 'setBatchSize'                | null                    || null
        'getMeasurementBatchSize'     | 'setBatchSize'                | 1                       || 1
        'getForkCount'                | 'setFork'                     | null                    || null
        'getForkCount'                | 'setFork'                     | 2                       || 2
        'getJvm'                      | 'setJvm'                      | null                    || null
        'getJvm'                      | 'setJvm'                      | 'myjvm'                 || 'myjvm'
        'getJvmArgs'                  | 'setJvmArgs'                  | null                    || null
        'getJvmArgs'                  | 'setJvmArgs'                  | ['Custom JVM args']     || ['Custom JVM args']
        'getJvmArgsAppend'            | 'setJvmArgsAppend'            | null                    || null
        'getJvmArgsAppend'            | 'setJvmArgsAppend'            | ['Custom JVM args']     || ['Custom JVM args']
        'getJvmArgsPrepend'           | 'setJvmArgsPrepend'           | null                    || null
        'getJvmArgsPrepend'           | 'setJvmArgsPrepend'           | ['Custom JVM args']     || ['Custom JVM args']
        'getResult'                   | 'setResultsFile'              | null                    || null
        'getResult'                   | 'setResultsFile'              | project.file('res.txt') || project.file('res.txt').absolutePath
        'getOperationsPerInvocation'  | 'setOperationsPerInvocation'  | null                    || null
        'getOperationsPerInvocation'  | 'setOperationsPerInvocation'  | 10                      || 10
        'getMeasurementTime'          | 'setTimeOnIteration'          | null                    || null
        'getMeasurementTime'          | 'setTimeOnIteration'          | '1s'                    || TimeValue.seconds(1)
        'getTimeout'                  | 'setTimeout'                  | null                    || null
        'getTimeout'                  | 'setTimeout'                  | '60s'                   || TimeValue.seconds(60)
        'getTimeUnit'                 | 'setTimeUnit'                 | null                    || null
        'getTimeUnit'                 | 'setTimeUnit'                 | 'ms'                    || TimeUnit.MILLISECONDS
        'getWarmupBatchSize'          | 'setWarmupBatchSize'          | null                    || null
        'getWarmupBatchSize'          | 'setWarmupBatchSize'          | 10                      || 10
        'getWarmupForkCount'          | 'setWarmupForks'              | null                    || null
        'getWarmupForkCount'          | 'setWarmupForks'              | 0                       || 0
        'getWarmupMode'               | 'setWarmupMode'               | null                    || null
        'getWarmupMode'               | 'setWarmupMode'               | 'INDI'                  || WarmupMode.INDI
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

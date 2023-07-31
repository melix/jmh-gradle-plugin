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

tasks.withType<Test>().configureEach {
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) = Unit

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent != null) {
                println("Test results ${project.name}: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)")
            }
        }

        override fun beforeTest(testDescriptor: TestDescriptor) {
            logger.lifecycle("Running test: $testDescriptor")
        }

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit
    })

    addTestOutputListener { testDescriptor, outputEvent ->
        logger.lifecycle("Test: $testDescriptor produced standard out/err: ${outputEvent.message}")
    }

    testLogging {
        showStandardStreams = true
    }

    useJUnitPlatform()

    // disabling Spock checks because Spock for whatever reason seems to consider the Groovy version of the
    // Gradle version under test to be processed, when it shouldn't
//    jvmArgs("-Dspock.iKnowWhatImDoing.disableGroovyVersionCheck=true")
}
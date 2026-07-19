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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class JMHPluginTest extends Specification {
    def "plugin is applied"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        then:
        def task = project.tasks.findByName('jmh')
        task instanceof JMHTask
    }

    def "plugin is applied with Groovy"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'groovy'
        project.apply plugin: 'me.champeau.jmh'

        then:
        def task = project.tasks.findByName('jmh')
        task instanceof JMHTask
    }

    void "plugin is applied wihtout zip64"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'groovy'
        project.apply plugin: 'me.champeau.jmh'

        then:
        def task = project.tasks.findByName('jmhJar')
        task instanceof Jar
        task.zip64 == false

    }

    
    def "plugin is applied with zip64"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'groovy'
        project.apply plugin: 'me.champeau.jmh'

        when:
        project.jmh.zip64.set(true)

        then:
        def task = project.tasks.findByName('jmhJar')
        task instanceof Jar
        task.zip64

    }

    def "all JMH tasks belong to the JMH group"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        then:
        project.tasks.find { it.name.startsWith('jmh') }.each {
            assert it.group == JMHPlugin.JMH_GROUP
        }
    }

    def "plugin is applied together with shadow plugin (#shadowPlugin)"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: shadowPlugin
        project.apply plugin: 'me.champeau.jmh'

        then:
        def task = project.tasks.findByName('jmhJar')
        task instanceof ShadowJar

        where:
        shadowPlugin << [
                'com.gradleup.shadow',
        ]
    }

    void "default duplicates strategy is to include"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        then:
        project.jmh.duplicateClassesStrategy.get() == DuplicatesStrategy.INCLUDE
    }

    @Unroll
    def "failOnError #value generates -foe #expected in JMH arguments"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        when:
        def task = project.tasks.findByName('jmh') as JMHTask
        task.failOnError.set(value)
        def args = []
        ParameterConverter.collectParameters(task, args)

        then:
        args.contains('-foe')
        args[args.indexOf('-foe') + 1] == expected

        where:
        value | expected
        true  | 'true'
        false | 'false'
    }

   def "list flags emit a bare JMH flag when true and nothing when false"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        when: 'all list flags are set to true'
        def task = project.tasks.findByName('jmh') as JMHTask
        task.listBenchmarks.set(true)
        task.listProfilers.set(true)
        task.listProfilersDetails.set(true)
        task.listResultFormats.set(true)
        def args = []
        ParameterConverter.collectParameters(task, args)

        then: 'presence-based flags are emitted as bare flags, with no true/false value token'
        ['-l', '-lp', '-lprof', '-lrf'].each { flag ->
            assert args.contains(flag)
            def idx = args.indexOf(flag)
            assert idx == args.size() - 1 || !(args[idx + 1] in ['true', 'false'])
        }

        when: 'a list flag is explicitly set to false and the others to true'
        def project2 = ProjectBuilder.builder().build()
        project2.repositories {
            mavenCentral()
        }
        project2.apply plugin: 'java'
        project2.apply plugin: 'me.champeau.jmh'
        def task2 = project2.tasks.findByName('jmh') as JMHTask
        task2.listBenchmarks.set(false)
        task2.listProfilers.set(true)
        task2.listProfilersDetails.set(true)
        task2.listResultFormats.set(true)
        def args2 = []
        ParameterConverter.collectParameters(task2, args2)

        then: 'only the false flag is suppressed; the other list flags remain'
        !args2.contains('-l')
        args2.contains('-lp')
        args2.contains('-lprof')
        args2.contains('-lrf')
    }

    def "absent list flags emit no JMH options"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        when:
        def task = project.tasks.findByName('jmh') as JMHTask
        def args = []
        ParameterConverter.collectParameters(task, args)

        then:
        !args.contains('-l')
        !args.contains('-lp')
        !args.contains('-lprof')
        !args.contains('-lrf')
    }

    def "jmhOptions are passed through verbatim to JMH arguments"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenCentral()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.jmh'

        when:
        def task = project.tasks.findByName('jmh') as JMHTask
        task.jmhOptions.set(['-l', '-lp', '-wbs', '5'])
        def args = []
        ParameterConverter.collectParameters(task, args)

        then:
        args.containsAll(['-l', '-lp', '-wbs', '5'])

        when: 'jmhOptions is set to an empty list'
        def project2 = ProjectBuilder.builder().build()
        project2.repositories {
            mavenCentral()
        }
        project2.apply plugin: 'java'
        project2.apply plugin: 'me.champeau.jmh'
        def task2 = project2.tasks.findByName('jmh') as JMHTask
        def baseline = []
        ParameterConverter.collectParameters(task2, baseline)
        task2.jmhOptions.set([])
        def args2 = []
        ParameterConverter.collectParameters(task2, args2)

        then: 'an empty jmhOptions list does not change the computed argument list'
        args2 == baseline
        when: 'jmhOptions contains an empty element'
        def project3 = ProjectBuilder.builder().build()
        project3.repositories {
            mavenCentral()
        }
        project3.apply plugin: 'java'
        project3.apply plugin: 'me.champeau.jmh'
        def task3 = project3.tasks.findByName('jmh') as JMHTask
        task3.jmhOptions.set(['-rf', 'json', ''])
        def args3 = []
        ParameterConverter.collectParameters(task3, args3)

        then: 'empty elements are filtered out, real options pass through'
        args3.containsAll(['-rf', 'json'])
        !args3.contains('')
    }
}

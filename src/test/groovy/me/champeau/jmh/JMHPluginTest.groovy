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
                'com.github.johnrengelman.shadow',
                'io.github.goooler.shadow'
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
}

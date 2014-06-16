/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.champeau.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.testfixtures.ProjectBuilder

class JMHPluginTest extends GroovyTestCase {
    void testPluginIsApplied() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'java'
        project.apply plugin: 'me.champeau.gradle.jmh'


        def task = project.tasks.findByName('jmh')
        assert task instanceof JavaExec

    }

    void testPluginIsAppliedWithGroovy() {
        Project project = ProjectBuilder.builder().build()
        project.repositories {
            mavenLocal()
            jcenter()
        }
        project.apply plugin: 'groovy'
        project.apply plugin: 'me.champeau.gradle.jmh'


        def task = project.tasks.findByName('jmh')
        assert task instanceof JavaExec

    }
}

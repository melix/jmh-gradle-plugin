/*
 * Copyright 2003-2012 the original author or authors.
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
package me.champeau.gradle;

import org.gradle.api.GradleException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;

import javax.inject.Inject;

public class IsolatedRunner implements Runnable {

    private final Options options;

    @Inject
    public IsolatedRunner(final Options options) {
        this.options = options;
    }

    @Override
    public void run() {
        Runner runner = new Runner(options);
        try {
            runner.run();
        } catch (RunnerException e) {
            throw new GradleException("Error during execution of benchmarks", e);
        }
    }
}

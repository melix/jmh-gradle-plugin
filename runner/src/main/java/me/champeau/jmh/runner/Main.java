/*
 * Copyright 2016 the original author or authors.
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
package me.champeau.jmh.runner;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Main {
    public static void main(String[] args) throws IOException, RunnerException, ClassNotFoundException {
        String optionsFile = args[0];
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(optionsFile));
        SerializableOptions options = (SerializableOptions) ois.readObject();
        ois.close();
        Runner runner = new Runner(options);
        runner.run();
    }
}

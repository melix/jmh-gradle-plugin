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
package me.champeau.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.Date;

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class JavaBenchmark {
    private HelperClassFromTestPackage helper;

    @Setup
    public void setUp() {
        System.out.println(org.apache.commons.lang3.time.FastDateFormat.getInstance().format(new Date()));
        System.out.println(new DateFormatter().format(new Date()));
        System.out.println(org.apache.commons.lang3.ClassUtils.getAbbreviatedName(getClass(), 15));
        helper = new HelperClassFromTestPackage(2.5);
    }

    @Benchmark
    public double sqrtBenchmark(){
        return helper.compute();
    }
}

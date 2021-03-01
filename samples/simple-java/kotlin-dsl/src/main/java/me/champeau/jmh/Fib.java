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

public class Fib {
    public static long fibClassic(int n) {
        if (n < 2) {
            return n;
        }
        return fibClassic(n - 1) + fibClassic(n - 2);
    }

    public static long tailRecFib(int n) {
        return tailRecFib(n, 0, 1);
    }

    private static int tailRecFib(int n, int a, int b) {
        if (n == 0) {
            return a;
        }
        if (n == 1) {
            return b;
        }
        return tailRecFib(n - 1, b, a + b);
    }
}

/*
 * Copyright 2014-2016 the original author or authors.
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
package me.champeau.gradle.jmh;

public class HelperClassFromTestPackage {
    private double value;
	
	public HelperClassFromTestPackage(double value) {
		this.value = value;
	}

    public double compute() {
        return org.apache.commons.lang3.math.IEEE754rUtils.min(value, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }
}

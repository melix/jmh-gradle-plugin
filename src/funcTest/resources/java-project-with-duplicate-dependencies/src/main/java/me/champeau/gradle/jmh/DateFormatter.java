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

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

public class DateFormatter {
    private static final FastDateFormat DATE_FORMATTER =
            FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT);

    public String format(Date date) {
        return DATE_FORMATTER.format(date);
    }
}

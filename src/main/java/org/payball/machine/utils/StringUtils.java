/*
 * Copyright 2018 Pablo Navais
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.payball.machine.utils;

import java.util.stream.IntStream;

/**
 * Provides generic String manipulation utilities
 */
public class StringUtils {

    /**
     * Creates a string by repeating a given
     * input pattern a number of times.
     *
     * @param s the string pattern to repeat
     * @param n the number of times to repeat
     * @return
     */
    public static String expand(String s, int n) {
        StringBuffer buffer = new StringBuffer();
        IntStream.range(0, n).forEach(v -> buffer.append(s));
        return buffer.toString();
    }

    /**
     * Shortens a given string by replacing its
     * exceeding characters with a group of dots.
     * @param s the string to shorten
     * @param limit the limit
     */
    public static String ellipsis(String s, int limit) {
        return (s.length()>limit) ? s.substring(0, limit)+"..." : s;
    }

    /**
     * Apply a padding to the given string using
     * the provided characters up to a certain length.
     *
     * @param s the string to pad
     * @param p the padding character
     * @param n the limit
     *
     * @return the padded string
     */
    public static String padd(String s, char p, int n) {
        StringBuffer buffer = new StringBuffer(s);
        IntStream.range(0, n-s.length()).forEach( v -> buffer.append(p));
        return buffer.toString();
    }
}

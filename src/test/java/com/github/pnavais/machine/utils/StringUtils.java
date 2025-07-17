/*
 * Copyright 2019 Pablo Navais
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
package com.github.pnavais.machine.utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * Provides generic String manipulation utilities
 */
public class StringUtils {

    /**
     * Creates a string by repeating a given
     * input pattern a number of times.
     *
     * @param c the string pattern to repeat
     * @param n the number of times to repeat
     */
    public static String expand(char c, int n) {
        StringBuffer buffer = new StringBuffer();
        IntStream.range(0, n).forEach(v -> buffer.append(c));
        return buffer.toString();
    }

    /**
     * Shortens a given string by replacing its
     * exceeding characters with a group of dots.
     * @param s the string to shorten
     * @param limit the limit
     */
    public static String ellipsis(String s, int limit) {
        return (s.length()>limit) ? s.substring(0, limit-3)+"..." : s;
    }

    /**
     * Apply a left padding to the given string using
     * the provided character up to a certain length.
     *
     * @param s the string to pad
     * @param p the padding character
     * @param n the limit
     *
     * @return the padded string
     */
    public static String padRight(String s, char p, int n) {
        StringBuffer buffer = new StringBuffer(s);
        IntStream.range(0, n-s.length()).forEach( v -> buffer.append(p));
        return buffer.toString();
    }

    /**
     * Formats a map into a string using the specific formatter
     *
     * @param <K>       the key type
     * @param <V>       the value type
     * @param formatter the key/value formatter
     * @param map       the map
     * @return the string representation of the map
     */
    public static  <K,V>  String formatMap(BiFunction<K, V, String> formatter, Map<K, V> map) {
        StringBuilder builder = new StringBuilder();
        AtomicReference<String> prefix = new AtomicReference<>(" ");
        builder.append("[");
        if (map != null) {
            map.forEach((k, v) -> {
                builder.append(prefix.get()).append(formatter.apply(k, v)).append(" ");
                prefix.set(", ");
            });
        }
        builder.append("]");
        return builder.toString();
    }
}

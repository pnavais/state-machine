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

package com.github.pnavais.machine.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.Locale;

/**
 * Contains utility method to translated colors to
 * string formats.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColorTranslator {

    /**
     * Convert a color to an HSB string
     *
     * @param color the color
     * @return the HSB representation
     */
    public static String toHSBColor(Color color) {
        float[] hsbValues = { 0.0f, 0.0f, 0.0f };
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbValues);
        return String.format(Locale.ROOT, "%.4f, %.4f, %.4f", hsbValues[0], hsbValues[1], hsbValues[2]);
    }

    /**
     * Convert a color to an RGB string
     *
     * @param color the color
     * @return the HSB representation
     */
    public static String toRGBColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()).toUpperCase();
    }
}

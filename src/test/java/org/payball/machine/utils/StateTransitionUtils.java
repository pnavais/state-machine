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

import org.payball.machine.machine.StateMachine;
import org.payball.machine.machine.api.Message;
import org.payball.machine.machine.model.State;
import org.payball.machine.machine.model.StateTransitionMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility methods for Transitions handling.
 */
public class StateTransitionUtils {

    /** The source tag displayed in the header */
    private static final String SOURCE_TAG = "Source";

    /** The message tag displayed in the header */
    private static final String MESSAGE_TAG = "Message";

    /** The target tag displayed in the header */
    private static final String TARGET_TAG = "Target";

    /**
     * Avoid external instantiation
     */
    private StateTransitionUtils() {}

    /**
     * Displays the transitions in a table
     * using the given PrintStream.
     *
     * @param transitionsMap the transitions
     */
    public static void printTransitions(StateTransitionMap transitionsMap) {
        printTransitions(transitionsMap,  StateTransitionPrintBuilder.getDefault());
    }

    /**
     * Displays the transitions in a table
     * using the given PrintStream.
     *
     * @param transitionsMap the transitions
     */
    public static void printTransitions(StateTransitionMap transitionsMap, StateTransitionPrint options) {
        if (transitionsMap != null) {
            final StateTransitionPrint printOptions = Optional.ofNullable(options).orElse(StateTransitionPrintBuilder.getDefault());
            Map<State, Map<Message, State>> map = transitionsMap.getTransitionMap();

            // Compute Header and margins
            String headerLine = StringUtils.expand("-", SOURCE_TAG.length()+MESSAGE_TAG.length()+TARGET_TAG.length()+((printOptions.getHeaderWidth() * 6 ) + 2));
            String spacer = StringUtils.expand(" ", printOptions.getHeaderWidth());

            // Print Header
            printOptions.getOutput().println("+"+headerLine+"+");
            printOptions.getOutput().print("|"+spacer+SOURCE_TAG+spacer+"|");
            printOptions.getOutput().print(spacer+MESSAGE_TAG+spacer+"|");
            printOptions.getOutput().println(spacer+TARGET_TAG+spacer+"|");
            printOptions.getOutput().println("+"+headerLine+"+");

            // Compute inner cell margin
            String innerSpace = StringUtils.expand(" ", 2);

            // Print transitions
            map.forEach((origin, tmap) ->
                tmap.forEach((message, target) -> {

                    int gap = (spacer.length() * 2) - (innerSpace.length() * 2);

                    printOptions.getOutput().print("|"+innerSpace + formatMessage(printOptions.getStateFormatter(), origin, gap, SOURCE_TAG, printOptions)   + innerSpace);
                    printOptions.getOutput().print("|"+innerSpace + formatMessage(printOptions.getMessageFormatter(), message, gap, MESSAGE_TAG, printOptions) + innerSpace);
                    printOptions.getOutput().println("|"+innerSpace + formatMessage(printOptions.getStateFormatter(), target, gap, TARGET_TAG, printOptions) + innerSpace+"|");
                    printOptions.getOutput().println("+"+headerLine+"+");

                })
            );

        }
    }

    /**
     * Formats the contents using its formatter for a given header and gap excess.
     * Print options are also supplied to help formatting.
     *
     * @param formatter the formatter
     * @param content the content to format
     * @param gap the excess margin
     * @param headerTag the header tag
     * @param printOptions the print options
     * @param <T> the type of the content
     * @return the formatted message
     */
    private static <T> String formatMessage(Function<T, String> formatter, T content, int gap, String headerTag, StateTransitionPrint printOptions) {
        // Shortens the text if exceeding header
        String shortenedMessage = StringUtils.ellipsis(formatter.apply(content), headerTag.length() + gap);
        String out = shortenedMessage;
        switch (printOptions.getCellAlignment()) {
            case LEFT:
                out = StringUtils.padRight(shortenedMessage, ' ', headerTag.length() + gap);
                break;
            case CENTER:
                out = StringUtils.center(shortenedMessage, ' ', headerTag.length() + gap);
                break;
            case RIGHT:
                out = StringUtils.padLeft(shortenedMessage, ' ', headerTag.length() + gap);
                break;
        }
        return out;

    }
}

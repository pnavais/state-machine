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

import org.payball.machine.api.AbstractNode;
import org.payball.machine.api.Message;
import org.payball.machine.model.State;
import org.payball.machine.model.StateTransitionMap;

import java.io.PrintStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility methods for Transitions handling.
 */
public class TransitionUtils {

    /** The source tag displayed in the header */
    private static final String SOURCE_TAG = "Source";

    /** The message tag displayed in the header */
    private static final String MESSAGE_TAG = "Message";

    /** The target tag displayed in the header */
    private static final String TARGET_TAG = "Target";

    /** The maximum spacing size in each header's column */
    private static final int HEADER_GAP_WIDTH = 20;


    /**
     * Displays the transitions in a table
     * using the given PrintStream.
     *
     * @param transitionsMap the transitions
     */
    public static void printTransitions(StateTransitionMap transitionsMap) {
        printTransitions(transitionsMap,  TransitionPrintBuilder.getDefault());
    }

    /**
     * Displays the transitions in a table
     * using the given PrintStream.
     *
     * @param transitionsMap the transitions
     */
    public static void printTransitions(StateTransitionMap transitionsMap, TransitionPrintOptions options) {
        if (transitionsMap != null) {
            final TransitionPrintOptions printOptions = Optional.ofNullable(options).orElse(TransitionPrintBuilder.getDefault());
            Map<State, Map<Message<?>, State>> map = transitionsMap.getTransitionMap();

            // Print Header
            String headerLine = StringUtils.expand("-", SOURCE_TAG.length()+MESSAGE_TAG.length()+TARGET_TAG.length()+((HEADER_GAP_WIDTH *6)+2));

            String spacer = StringUtils.expand(" ", HEADER_GAP_WIDTH);
            printOptions.getOutput().println("+"+headerLine+"+");
            printOptions.getOutput().print("|"+spacer+SOURCE_TAG+spacer+"|");
            printOptions.getOutput().print(spacer+MESSAGE_TAG+spacer+"|");
            printOptions.getOutput().println(spacer+TARGET_TAG+spacer+"|");
            printOptions.getOutput().println("+"+headerLine+"+");

            // Print transitions
            map.forEach((origin, tmap) ->
                tmap.forEach((message, target) -> {
                    printOptions.getOutput().print("|  " + StringUtils.center(StringUtils.ellipsis(printOptions.getStateFormatter().apply(origin), SOURCE_TAG.length() + (spacer.length()*2) - 4),
                                    ' ', SOURCE_TAG.length() + (spacer.length()*2) - 4) + "  |");
                    printOptions.getOutput().print("  " + StringUtils.center(StringUtils.ellipsis(message.getPayload().toString(),
                                    MESSAGE_TAG.length() + (spacer.length()*2) - 4),
                                    ' ', MESSAGE_TAG.length() + (spacer.length()*2) - 4) + "  |");
                    printOptions.getOutput().println("  " + StringUtils.center(StringUtils.ellipsis(printOptions.getStateFormatter().apply(target), TARGET_TAG.length() + (spacer.length()*2) - 4), ' ',
                                    TARGET_TAG.length() + (spacer.length()*2) - 4) + "  |");
                    printOptions.getOutput().println("+"+headerLine+"+");
                })
            );

        }
    }
}

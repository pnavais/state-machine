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

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.ColumnData;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.index.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
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
     * using default print options.
     *
     * @param transitionsMap the transitions
     */
    public static void printTransitions(StateTransitionMap transitionsMap) {
        printTransitions(transitionsMap,  StateTransitionPrintBuilder.getDefault());
    }

    /**
     * Displays the transitions in a compact table using
     * default print options.
     *
     * @param transitionsMap the transitions
     */
    public static void printShortTransitions(StateTransitionMap transitionsMap) {
        printShortTransitions(transitionsMap,  StateTransitionPrintBuilder.getDefault());
    }

    /**
     * Displays the transitions in a table
     * using the given transition print options.
     *
     * @param transitionsMap the transitions
     */
    public static void printTransitions(StateTransitionMap transitionsMap, StateTransitionPrint options) {
        List<StateRow> data = new ArrayList<>();
        if (transitionsMap != null) {
            transitionsMap.getTransitionMap().forEach((source, messageStateMap) -> messageStateMap.forEach((message, target) -> {
                data.add(StateRow.builder().source(source).message(message).target(target).build());
            }));
        }

        System.out.println(TitledAsciiTable.getTable("State Transitions", data, Arrays.asList(
                new AlignedColumn(options.getCellAlignment()).header("Source").with(stateRow -> options.getStateFormatter().apply(stateRow.getSource())),
                new AlignedColumn(options.getCellAlignment()).header("Message").with(stateRow -> options.getMessageFormatter().apply(stateRow.getMessage())),
                new AlignedColumn(options.getCellAlignment()).header("Target").with(stateRow -> options.getStateFormatter().apply(stateRow.getTarget())))));
    }

    /**
     * Displays the transitions in a compact format.
     *
     * @param transitionsMap the transitions
     */
    public static void printShortTransitions(StateTransitionMap transitionsMap, StateTransitionPrint options) {
        List<StateRow> data = new ArrayList<>();

        if (transitionsMap != null) {
            transitionsMap.getTransitionMap().forEach((source, transitions) -> data.add(StateRow.builder().source(source).transitions(transitions).build()));
        }
        System.out.println(TitledAsciiTable.getTable("State Transitions", data, Arrays.asList(
                new AlignedColumn(options.getCellAlignment()).header("Source").with(stateRow -> options.getStateFormatter().apply(stateRow.getSource())),
                new AlignedColumn(options.getCellAlignment()).header("Target").maxColumnWidth(options.getTableWidth()).with(stateRow -> StringUtils.formatMap(options.getMapFormatter(), stateRow.getTransitions())))));
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
        String message = formatter.apply(content);
        String shortenedMessage = printOptions.isUseEllipsis() ? StringUtils.ellipsis(message, headerTag.length() + gap) : message;
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

    /**
     * A utility helper class to hold state rows
     */
    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    private static class StateRow {
        /** The source state */
        State source;

        /** The message */
        Message message;

        /** The target state */
        State target;

        /** The transitions */
        Map<Message, State> transitions;
    }

    /**
     * A custom Column extension to translate and apply
     * alignments to both data and header.
     */
    private static class AlignedColumn extends Column {

        /**
         * Constructor with alignment option
         * @param cellAlignment the alignment option
         */
        public AlignedColumn(StateTransitionPrint.CellAlignment cellAlignment) {
            HorizontalAlign align = HorizontalAlign.valueOf(cellAlignment.name());
            headerAlign(align).dataAlign(align);
        }

    }

    /**
     * Adds a title to the rendered table
     */
    private static class TitledAsciiTable {

        /**
         * Creates the table
         *
         * @param <T>     the type parameter
         * @param title the table's title
         * @param objects the objects
         * @param columns the columns
         * @return the table
         */
        public static <T> String getTable(String title, Collection<T> objects, List<ColumnData<T>> columns) {
            String renderedTable = AsciiTable.getTable(objects, columns);
            int maxWidth = renderedTable.indexOf(System.lineSeparator());

            title = StringUtils.ellipsis(title, maxWidth-4);

            String separatorRow = renderedTable.substring(0, maxWidth);
            String contentRow = renderedTable.substring(maxWidth+2, maxWidth*2+2);
            String spacerRow = StringUtils.expand(separatorRow.charAt(1), contentRow.length()-2);
            String spacer = StringUtils.expand(' ', ((contentRow.length()-2)/2) - (title.length()/2));
            String titleRow = StringUtils.padRight(spacer + title, ' ', maxWidth-2);

            System.out.println(separatorRow.charAt(0)+spacerRow+separatorRow.charAt(separatorRow.length()-1));
            System.out.println(contentRow.charAt(0)+titleRow+contentRow.charAt(contentRow.length()-1));

            return renderedTable;
        }

    }
}

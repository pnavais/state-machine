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
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.model.State;
import lombok.*;

import java.util.*;

/**
 * Utility methods for Transitions handling.
 */
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StateTransitionPrinter<S extends State, M extends Message, T extends Transition<S,M>>  {

    /** The source tag displayed in the header */
    private static final String SOURCE_TAG = "Source";

    /** The message tag displayed in the header */
    private static final String MESSAGE_TAG = "Message";

    /** The target tag displayed in the header */
    private static final String TARGET_TAG = "Target";

    /** The state transitions table title */
    @Builder.Default private final String title = "State Transitions";

    /** The compact mode flag */
    @Builder.Default private boolean compactMode = false;

    @Builder.Default private StateTransitionPrintOptions<S, M> printOptions = StateTransitionPrintOptions.<S, M>builder().build().fillDefaults();

    /**
     * Displays the transitions in a table
     * using default print options and title.
     *
     * @param transitionsMap the transitions
     */
    public String printTransitions(TransitionIndex<S, M, T> transitionsMap) {
        return printTransitions(transitionsMap, title, printOptions, compactMode);
    }

    /**
     * Displays the transitions in a table
     * using the given title and transition
     * print options.
     *
     * @param transitionsMap the transitions
     * @param title the title of the table
     * @param options the print options
     * @param compactMode use compact mode
     */
    public static <S extends State, M extends Message, T extends Transition<S,M>> String printTransitions(TransitionIndex<S, M, T> transitionsMap, String title, StateTransitionPrintOptions<S,M> options, boolean compactMode) {
        return (compactMode) ? printCompact(title, transitionsMap, options) : printNormal(title, transitionsMap, options);
    }

    /**
     * Displays the transitions in a table
     * using the given title and transition
     * print options.
     *
     * @param title the title of the table
     * @param transitionsMap the transitions
     * @param options the print options
     */
    public static <S extends State, M extends Message, T extends Transition<S,M>> String printNormal(String title, TransitionIndex<S, M, T> transitionsMap, StateTransitionPrintOptions<S,M> options) {
        List<StateRow<S,M>> data = new ArrayList<>();
        if (transitionsMap != null) {
            transitionsMap.getTransitionsAsMap().forEach((source, messageStateMap) -> messageStateMap.forEach((message, target) -> {
                data.add(StateRow.<S,M>builder().source(source).message(message).target(target).build());
            }));
        }

        String renderedTable = TitledAsciiTable.getTable(title, data, Arrays.asList(
                new AlignedColumn(options.getCellAlignment()).header("Source").with(stateRow -> options.getStateFormatter().apply(stateRow.getSource())),
                new AlignedColumn(options.getCellAlignment()).header("Message").with(stateRow -> options.getMessageFormatter().apply(stateRow.getMessage())),
                new AlignedColumn(options.getCellAlignment()).header("Target").with(stateRow -> options.getStateFormatter().apply(stateRow.getTarget()))));

        options.getOutput().println(renderedTable);
        return renderedTable;
    }

    /**
     * Displays the transitions in a compact format with
     * a title and specific display options.
     *
     * @param title the title of the table
     * @param transitionsMap the transitions
     * @param options the print options
     */
    private static <S extends State, M extends Message, T extends Transition<S,M>> String printCompact(String title, TransitionIndex<S, M, T> transitionsMap, StateTransitionPrintOptions<S,M> options) {
        List<StateRow<S, M>> data = new ArrayList<>();

        if (transitionsMap != null) {
            transitionsMap.getTransitionsAsMap().forEach((source, transitions) -> data.add(StateRow.<S, M>builder().source(source).transitions(transitions).build()));
        }
        String renderedTable = TitledAsciiTable.getTable(title, data, Arrays.asList(
                new AlignedColumn(options.getCellAlignment()).header("Source").with(stateRow -> options.getStateFormatter().apply(stateRow.getSource())),
                new AlignedColumn(options.getCellAlignment()).header("Target").maxColumnWidth(options.getTableWidth()).with(stateRow -> StringUtils.formatMap(options.getMapFormatter(), stateRow.getTransitions()))));

        options.getOutput().println(renderedTable);

        return renderedTable;
    }

    /**
     * A utility helper class to hold state rows
     */
    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    private static class StateRow<S extends State, M extends Message> {
        /** The source state */
        S source;

        /** The message */
        M message;

        /** The target state */
        S target;

        /** The transitions */
        Map<M, S> transitions;
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
        public AlignedColumn(StateTransitionPrintOptions.CellAlignment cellAlignment) {
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
         * @param <T>   the type parameter
         * @param title the table's title
         * @param objects the objects
         * @param columns the columns
         * @return the table
         */
        public static <T> String getTable(String title, Collection<T> objects, List<ColumnData<T>> columns) {

            StringBuilder titleTableBuilder = new StringBuilder();
            String renderedTable = AsciiTable.getTable(objects, columns);

            // Append title if any
            if (title != null) {
                String lineSeparator = System.lineSeparator();
                int sepLength = lineSeparator.length();
                int maxWidth = renderedTable.indexOf(lineSeparator);

                title = StringUtils.ellipsis(title, maxWidth - 4);

                char cornerChar = renderedTable.charAt(0);
                char separatorChar = renderedTable.charAt(1);
                char rowSeparatorChar = renderedTable.charAt(maxWidth + sepLength);

                 titleTableBuilder.append(cornerChar).append(StringUtils.expand(separatorChar, maxWidth - 2)).append(cornerChar);
                String contentRow = StringUtils.expand(' ', (maxWidth - 2) / 2 - title.length() / 2) + title;
                titleTableBuilder.append(lineSeparator).append(rowSeparatorChar);
                titleTableBuilder.append(StringUtils.padRight(contentRow, ' ', maxWidth - 2));
                titleTableBuilder.append(rowSeparatorChar);
                titleTableBuilder.append(lineSeparator);
            }

            titleTableBuilder.append(renderedTable);
            return titleTableBuilder.toString();
        }
    }
}

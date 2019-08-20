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

package com.github.pnavais.machine.exporter;

import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.model.State;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

import java.awt.*;
import java.nio.file.FileSystem;
import java.util.Map;
import java.util.stream.IntStream;


/**
 * An exporter allowing to translate a given state machine representation
 * (States and transitions) to YAML.
 */
@Log
@Getter
@Setter
@NoArgsConstructor
public class YAMLExporter extends AbstractStatesExporter<String, State, Message, StateMachine> {

    /** Margin of 4 spaces */
    private static final String MARGIN_2_PTS = "  ";

    /** Margin of 4 spaces */
    private static final String MARGIN_4_PTS = MARGIN_2_PTS + MARGIN_2_PTS;

    /**
     * The margin spacing options (2 or 4 spaces)
     */
    public enum MarginSize {

        TWO_SP(MARGIN_2_PTS),
        FOUR_SP(MARGIN_4_PTS);

        private String value;

        MarginSize(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /** The margin size */
    private MarginSize marginSize = MarginSize.FOUR_SP;

    /**
     * All arguments constructor
     *
     * @param fileSystem the filesystem
     * @param graphName the graph name
     * @param finalStateColor the final state color
     * @param useHSB the flag to control exporting using HSB color format
     * @param showCurrent the flag to control annotating current status
     * @param currentStateColor the current state color
     */
    @Builder
    public YAMLExporter(FileSystem fileSystem, String graphName, Color finalStateColor, boolean useHSB, boolean showCurrent, Color currentStateColor,MarginSize marginSize) {
        super(fileSystem, graphName, finalStateColor, useHSB, showCurrent, currentStateColor);
        setMarginSize(marginSize == null ? this.marginSize : marginSize);
    }


    /**
     * Export the current contents of the state machine
     * to YAML syntax.
     *
     * @param stateMachine the state machine to export
     * @return the string representation of the state machine
     * in YAML.
     */
    @Override
    public String export(StateMachine stateMachine) {
        StringBuilder builder = new StringBuilder();
        appendNodes(builder, stateMachine);
        appendTransitions(builder, stateMachine);

        return builder.toString();
    }

    /**
     * Append all state attributes to the builder
     *
     * @param builder the builder
     * @param stateMachine the state machine
     */
    private void appendNodes(StringBuilder builder, StateMachine stateMachine) {
        builder.append("states:").append(NL);
        Map<State, Map<Message, State>> transitions = stateMachine.getTransitionsIndex().getTransitionsAsMap();
        for (State s : transitions.keySet()) {
            builder.append(marginSize)
                    .append("- state:").append(NL);
            printMargin(builder, 3);
            builder.append("name: ")
                    .append("\"")
                    .append(s.getName())
                    .append("\"").append(NL);

            if (isShowCurrent() && s.equals(stateMachine.getCurrent())) {
                printMargin(builder, 3);
                builder.append("current: \"true\"").append(NL);
            }

            if (s.isFinal()) {
                printMargin(builder, 3);
                builder.append("final: \"true\"").append(NL);
            }
            appendNodeProperties(builder, s);
        }
    }

    /**
     * Append all state transitions contained in the
     * state machine to the builder
     *
     * @param builder the builder
     * @param stateMachine the state machine
     */
    private void appendTransitions(StringBuilder builder, StateMachine stateMachine) {
        Map<State, Map<Message, State>> transitions = stateMachine.getTransitionsIndex().getTransitionsAsMap();

        if (!transitions.isEmpty()) {
            builder.append("transitions:");
        }

        transitions.keySet().forEach(source ->
                transitions.get(source).forEach((message, target) -> {
                    builder.append(NL).append(getMarginSize()).append("- transition:")
                            .append(NL);
                    printMargin(builder, 3);
                    builder.append("source: ");
                    builder.append("\"")
                            .append(source.getName())
                            .append("\"").append(NL);
                    printMargin(builder, 3);
                    builder.append("target: ");
                    builder.append("\"")
                            .append(target.getName())
                            .append("\"");
                    formatMessage(builder, message);
                }));
    }

    /**
     * Formats the given message
     *
     * @param builder the builder
     * @param message the message
     */
    private void formatMessage(StringBuilder builder, Message message) {
        // Ignore empty messages
        if ((!message.equals(Messages.EMPTY)) && (message.toString() != null))
        {
            builder.append(NL);
            printMargin(builder, 3);
            if (message.equals(Messages.ANY)) {
                builder.append("any: \"true\"");
            } else {
                builder.append("message: ")
                        .append("\"")
                        .append(message)
                        .append("\"");
            }
        }
    }

    /**
     * Appends the node properties as a dictionary to the builder
     *
     * @param builder the builder
     * @param state the state
     */
    private void appendNodeProperties(StringBuilder builder, State state) {
        if (state.hasProperties()) {
            printMargin(builder, 3);
            builder.append("properties:").append(NL);
            state.getProperties().forEach((k, v) -> {
                printMargin(builder, 4);
                builder.append(formatKey(k)).append(":").append(" \"");
                builder.append(v).append("\"").append(NL);
            });
        }
    }

    /**
     * Transforms the key into a portable format.
     * Actually, it converts all possible white spaces into underscores
     *
     * @param key the key
     * @return the formatted key
     */
    private String formatKey(String key) {
        return key.replaceAll("[\\s]", "_");
    }

    /**
     * Prints a margin of a maximum configurable size
     *
     * @param builder the builder
     * @param max the size of the margin
     */
    private void printMargin(StringBuilder builder, int max) {
        max = Math.max(max, 0);
        IntStream.range(1,max+1).forEach(i -> builder.append(getMarginSize()));
    }
}

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
import com.github.pnavais.machine.exporter.util.ColorTranslator;
import com.github.pnavais.machine.model.State;
import lombok.*;
import lombok.extern.java.Log;

import java.awt.*;
import java.nio.file.FileSystem;
import java.util.Map;

/**
 * An exporter allowing to translate
 * a given state machine to the GraphViz DOT language
 * (@see https://www.graphviz.org/doc/info/lang.html)
 */
@Log
@Getter
@Setter
@NoArgsConstructor
public class DOTExporter extends AbstractStatesExporter<String, State, Message, StateMachine> {

    /**
     * The possible Rank direction of the grapth
     */
    public enum RankDir {
        LR("rankdir=\"LR\";"), /* Left to right */
        TB("rankdir=\"TB\";"); /* Top to bottom */

        private String value;

        RankDir(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /** The graph direction */
    private RankDir rankDir = RankDir.LR;

    /**
     * All arguments constructor
     *
     * @param fileSystem the filesystem
     * @param graphName the graph name
     * @param finalStateColor the final state color
     * @param useHSB the flag to control exporting using HSB color format
     * @param showCurrent the flag to control annotating current status
     * @param currentStateColor the current state color
     * @param rankDir the graph direction
     */
    @Builder
    public DOTExporter(FileSystem fileSystem, String graphName, Color finalStateColor, boolean useHSB, boolean showCurrent, Color currentStateColor, RankDir rankDir) {
        super(fileSystem, graphName, finalStateColor, useHSB, showCurrent, currentStateColor);
        setRankDir((rankDir != null) ? rankDir : this.rankDir);
    }

    /**
     * Export the current contents of the state machine
     * to the DOT language.
     *
     * @param stateMachine the state machine to export
     * @return the string representation of the state machine
     * in DOT language.
     */
    @Override
    public String export(@NonNull StateMachine stateMachine) {
        StringBuilder builder = new StringBuilder("digraph ");
        builder.append(getGraphName());
        builder.append(" {");
        builder.append(NL);
        builder.append(TB).append(getRankDir()).append(NL);
        appendNodesDescription(stateMachine, builder);
        appendTransitions(stateMachine, builder);

        return builder.append("}").toString();
    }

    /**
     * Appends the node properties of the state machine.
     *
     * @param stateMachine the state machine
     * @param builder the builder
     */
    private void appendNodesDescription(StateMachine stateMachine, StringBuilder builder) {
        Map<State, Map<Message, State>> transitions = stateMachine.getTransitionsIndex().getTransitionsAsMap();
        for (State s : transitions.keySet()) {
            String prefix = TB + s.getName() + " [";

            prefix = formatCurrentNodeAttributes(stateMachine, builder, s, prefix);
            prefix = formatNodeProperties(builder, s, prefix);

            if (prefix.equals("")) {
                builder.append("];").append(NL);
            }
        }
    }

    /**
     * Format current node colors depending on its attributes
     *
     * @param stateMachine the state machine
     * @param builder      the builder
     * @param state        the state
     * @param prefix       the prefix
     * @return the properties as a string
     */
    private String formatCurrentNodeAttributes(StateMachine stateMachine, StringBuilder builder, State state, String prefix) {
        if ((state.isFinal() && (!state.hasProperty("color")))) {
            builder.append(prefix).append("style=\"filled\", fillcolor=\"").append(toOutputColor(getFinalStateColor())).append("\"");
            prefix = "";
        }

        if (isShowCurrent() && (state.equals(stateMachine.getCurrent())) && (!state.hasProperty("color"))) {
            prefix = prefix.equals("") ? ", " : prefix;
            builder.append(prefix).append("color=\"").append(toOutputColor(getCurrentStateColor())).append("\"");
            prefix = "";
        }

        // Add final label (ignored by DOT)
        if (state.isFinal()) {
            prefix = prefix.equals("") ? ", " : prefix;
            builder.append(prefix).append("final=\"true\"");
            prefix = "";
        }

        // Add current label (ignored by DOT)
        if (isShowCurrent() && state.equals(stateMachine.getCurrent())) {
            prefix = prefix.equals("") ? ", " : prefix;
            builder.append(prefix).append("current=\"true\"");
            prefix = "";
        }

        return prefix;
    }

    /**
     * Appends the node internal properties.
     *
     * @param builder the builder
     * @param state   the state
     * @param prefix  the prefix
     * @return the string
     */
    private String formatNodeProperties(StringBuilder builder, State state, String prefix) {
        if (state.hasProperties()) {
            prefix = prefix.equals("") ? ", " : prefix;
            builder.append(prefix);
            prefix = "";
            final String[] finalPrefix = { prefix };
            state.getProperties().keySet().stream().map(k -> k + "=\"" + state.getProperties().get(k) + "\"").forEachOrdered(p -> {
                builder.append(finalPrefix[0]).append(p);
                finalPrefix[0] = ", ";
            });
        }
        return prefix;
    }

    /**
     * Appends the node transitions of the state machine.
     *
     * @param stateMachine the state machine
     * @param builder the builder
     */
    private void appendTransitions(StateMachine stateMachine, StringBuilder builder) {
        stateMachine.getAllTransitions().forEach(t ->
                builder.append(TB).append(String.format("%s -> %s", t.getOrigin().getName(), t.getTarget().getName()))
                        .append(formatMessage(t.getMessage()))
                        .append(NL));
    }

    /**
     * Retrieves the formatted representation of the message
     * depending on its nature and contents.
     *
     * @param m the message
     * @return the formatted representation of the message.
     */
    private String formatMessage(Message m) {
        String formattedMessage = null;
        if (m.equals(Messages.ANY)) {
            formattedMessage = "*";
        } else if ((!m.equals(Messages.EMPTY)) && (!m.equals(Messages.NULL))) {
            Object payload = (m.getPayload() != null) ? m.getPayload().get() : null;
            formattedMessage = (payload == null) ? m.toString() : payload.toString();
        }

        return (formattedMessage == null) ? "" : " [label=\""+formattedMessage+"\"];";
    }

    /**
     * Translates the color to and HSB/RGB string representation
     *
     * @param color the color
     * @return the HSB/RGB string representation
     */
    private String toOutputColor(Color color) {
        return (isUseHSB()) ? ColorTranslator.toHSBColor(color) : ColorTranslator.toRGBColor(color);
    }


}

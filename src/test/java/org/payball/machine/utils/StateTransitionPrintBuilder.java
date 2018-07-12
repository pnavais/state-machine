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

import org.payball.machine.api.Message;
import org.payball.machine.model.State;

import java.io.PrintStream;
import java.util.function.Function;

/**
 * A simple builder to create State Transition
 * print options.
 */
public class StateTransitionPrintBuilder {

    /** The instance to be built */
    private StateTransitionPrint instance;

    /**
     * Private constructor avoiding instantiation
     */
    private StateTransitionPrintBuilder() {
        instance = new StateTransitionPrint();
    }

    /**
     * Creates a new builder with default
     * print options.
     *
     * @return the new builder
     */
    public static StateTransitionPrintBuilder newBuilder() {
        return new StateTransitionPrintBuilder();
    }

    /**
     * Sets the State formatter
     *
     * @param stateFormatter the sate formatter
     * @return the builder for chaining purposes
     */
    public StateTransitionPrintBuilder setStateFormatter(Function<State, String> stateFormatter) {
        instance.setStateFormatter(stateFormatter);
        return this;
    }

    /**
     * Sets the output stream
     *
     * @param output the output stream
     * @return the builder for chaining purposes
     */

    public StateTransitionPrintBuilder setOutput(PrintStream output) {
        instance.setOutput(output);
        return this;
    }

    /**
     * Sets the Message formatter
     *
     * @param messageFormatter the message formatter
     * @return the builder for chaining purposes
     */
    public StateTransitionPrintBuilder setMessageFormatter(Function<Message, String> messageFormatter) {
        instance.setMessageFormatter(messageFormatter);
        return this;
    }

    /**
     * Sets the header width
     *
     * @param width the header width
     * @return the builder for chaining purposes
     */
    public StateTransitionPrintBuilder setHeaderWidth(int width) {
        instance.setHeaderWidth(width);
        return this;
    }

    /**
     * Sets the cell alignment
     *
     * @param alignment the cell alignment
     * @return the builder for chaining purposes
     */
    public StateTransitionPrintBuilder setCellAlignment(StateTransitionPrint.Alignment alignment) {
        instance.setCellAlignment(alignment);
        return this;
    }

    /**
     * Returns the {@link StateTransitionPrint} instance
     * currently stored by the builder
     *
     * @return the StateTranstionPrint instance
     */
    public StateTransitionPrint build() {
        return instance;
    }

    /**
     * Retrieves the default Print option instance.
     * Contains simple state/message formatter and outputs to stdout.
     *
     * @return the default Print option instance
     */
    public static StateTransitionPrint getDefault() {
        return DefaultPrintOptionsHolder.instance;
    }

    /**
     * Initialization-on-demand holder to return the singleton
     * default Print option instance in a lazy fashion
     */
    private static class DefaultPrintOptionsHolder {
        private static StateTransitionPrint instance = new StateTransitionPrint();
    }

}

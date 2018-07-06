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

import org.payball.machine.machine.api.Message;
import org.payball.machine.machine.model.State;

import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * A simple holder containing print properties
 * to be used while creating state transitions
 * output.
 */
public class StateTransitionPrint {

    /** The possible text alignments */
    public enum Alignment { LEFT, CENTER, RIGHT };

    /** The maximum spacing size in each header's column */
    private static final int DEFAULT_HEADER_GAP_WIDTH = 20;

    /** The state formatter function */
    private Function<State, String> stateFormatter;

    /** The output stream */
    private PrintStream output;

    /** The message formatter function */
    private Function<Message, String> messageFormatter;

    /** The header width */
    private int headerWidth;

    /** The cell alignment */
    private Alignment cellAlignment;

    /**
     * Creates a StateTransitionPrint object
     * with default options.
     */
    public StateTransitionPrint() {
        stateFormatter = State::getName;
        output = System.out;
        messageFormatter = message -> Optional.ofNullable(message.getPayload()).orElse(()-> "[null]").get().toString();
        headerWidth = DEFAULT_HEADER_GAP_WIDTH;
        cellAlignment = Alignment.CENTER;
    }

    /**
     * Retrieves the state formatter
     *
     * @return the state formatter
     */
    public Function<State, String> getStateFormatter() {
        return stateFormatter;
    }

    /**
     * Sets the state formatter
     * @param stateFormatter the state formatter
     */
    public void setStateFormatter(Function<State, String> stateFormatter) {
        this.stateFormatter = stateFormatter;
    }

    /**
     * Retrieves the output stream
     *
     * @return the output stream
     */
    public PrintStream getOutput() {
        return output;
    }

    /**
     * Sets the output stream
     *
     * @param output the output stream
     */
    public void setOutput(PrintStream output) {
        this.output = output;
    }

    /**
     * Retrieves the message formatter
     *
     * @return the message formatter
     */
    public Function<Message, String> getMessageFormatter() {
        return messageFormatter;
    }

    /**
     * Sets the message formatter
     *
     * @param messageFormatter the message formatter
     */
    public void setMessageFormatter(Function<Message, String> messageFormatter) {
        this.messageFormatter = messageFormatter;
    }

    /**
     * Retrieves the header width
     *
     * @return the header width
     */
    public int getHeaderWidth() {
        return headerWidth;
    }

    /**
     * Sets the header width
     *
     * @param headerWidth the header width
     */
    public void setHeaderWidth(int headerWidth) {
        this.headerWidth = headerWidth;
    }

    /**
     * Retrieves the cell alignment
     *
     * @return the cell alignment
     */
    public Alignment getCellAlignment() {
        return cellAlignment;
    }

    /**
     * Sets the cell alignment
     *
     * @param cellAlignment the cell alignment
     */
    public void setCellAlignment(Alignment cellAlignment) {
        this.cellAlignment = cellAlignment;
    }
}

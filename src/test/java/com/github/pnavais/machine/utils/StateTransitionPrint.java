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

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.model.State;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.PrintStream;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A simple holder containing print properties
 * to be used while creating state transitions
 * output.
 */
@Getter
@Setter
@Builder(toBuilder=true)
@AllArgsConstructor
public class StateTransitionPrint {

    public enum CellAlignment { LEFT, RIGHT, CENTER }

    /** The maximum spacing size in each header's column */
    private static final int DEFAULT_TABLE_WIDTH = 140;

    /** The state formatter function */
    private Function<State, String> stateFormatter;

    /** The message formatter function */
    private Function<Message, String> messageFormatter;

    /** The Transitions map formatter function */
    private BiFunction<Message, State, String> mapFormatter;

    /** The output stream */
    private PrintStream output;

    /** The table width */
    private int tableWidth;

    /** Flag to use ellipsis if exceeding width */
    private boolean useEllipsis;

    /** The cell alignment */
    private CellAlignment cellAlignment;

    /**
     * Creates a StateTransitionPrint object
     * with default options.
     */
    public StateTransitionPrint() {
        stateFormatter = State::getName;
        messageFormatter = message -> Optional.ofNullable(message.getPayload()).orElse(()-> "[null]").get().toString();
        mapFormatter = (message, state) -> Optional.ofNullable(message.getPayload()).orElse(()-> "[null]").get().toString() + " -> " + stateFormatter.apply(state);
        output = System.out;
        tableWidth = DEFAULT_TABLE_WIDTH;
        cellAlignment = CellAlignment.CENTER;
        useEllipsis = true;
    }

}

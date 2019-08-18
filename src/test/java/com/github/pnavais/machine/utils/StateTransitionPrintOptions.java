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

import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.model.State;
import lombok.*;

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
public class StateTransitionPrintOptions<S extends State, M extends Message> {

    /** The cell alignment */
    public enum CellAlignment { LEFT, RIGHT, CENTER }

    /** The maximum spacing size in each header's column */
    private static final int DEFAULT_TABLE_WIDTH = 140;

    /** The state formatter function */
    @Builder.Default private Function<S, String> stateFormatter = s -> s.isFinal() ? s.getName()+"*" : s.getName();

    /** The message formatter function */
    @Builder.Default private Function<M, String> messageFormatter = message -> Optional.ofNullable(message.getPayload()).orElse(()-> "[null]").get().toString();

    /** The Transitions map formatter function */
    private BiFunction<M, S, String> mapFormatter;

    /** The output stream */
    @Builder.Default private PrintStream output = System.out;

    /** The table width */
    @Builder.Default private int tableWidth = DEFAULT_TABLE_WIDTH;

    /** The cell alignment */
    @Builder.Default private CellAlignment cellAlignment = CellAlignment.CENTER;

    /**
     * Instantiates a new State transition print.
     */
    public StateTransitionPrintOptions() {
        fillDefaults();
    }

    /**
     * Fills the default values for dependent fields not carried by the builder
     */
    public StateTransitionPrintOptions<S,M> fillDefaults() {
        if (mapFormatter == null) {
            mapFormatter = (message, state) -> Optional.ofNullable(message.getPayload()).orElse(()-> "[null]").get().toString() + " -> " + stateFormatter.apply(state);
        }
        return this;
    }
}

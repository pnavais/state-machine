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
package com.github.pnavais.machine;

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.utils.StateTransitionPrint;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for state machine tests
 */
public abstract class AbstractStateMachineTest {

    /** The state printer */
    private static StateTransitionPrint<State, Message> statePrinter;

    @BeforeAll
    public static void init() {
        statePrinter = StateTransitionPrint.builder()
                .stateFormatter(s -> s.getName()+" ["+s.getId()+"]")
                .build().fillDefaults();
    }

    /**
     * Retrieves the state printer.
     *
     * @return the state printer
     */
    public static StateTransitionPrint<State,Message> getStatePrinter() {
        return statePrinter;
    }
}

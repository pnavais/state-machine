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
package org.payball.machine.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.payball.machine.machine.StateMachine;
import org.payball.machine.machine.builder.StateMachineBuilder;
import org.payball.machine.machine.model.StateTransition;
import org.payball.machine.utils.StateTransitionPrint;
import org.payball.machine.utils.StateTransitionPrintBuilder;

/**
 * Base class for state machine tests
 */
public abstract class AbstractStateMachineTest {

    /** Default state machine for tests */
    StateMachine defaultMachine;

    /** The state printer */
    static StateTransitionPrint statePrinter;

    @BeforeAll
    public static void init() {
        statePrinter = StateTransitionPrintBuilder.newBuilder()
                .setStateFormatter(s -> s.getName()+" ["+s.getId()+"]")
                .setHeaderWidth(20).build();
    }

    @BeforeEach
    public void resetMachine() {
        defaultMachine.init();
    }

    /**
     * Creates the default State Machine
     */
    AbstractStateMachineTest() {
        defaultMachine = StateMachineBuilder.newBuilder()
                .add(StateTransition.of("A","B","1"))
                .add(StateTransition.of("B", "C", "2"))
                .add(StateTransition.of("C", "D", "3"))
                .add(StateTransition.of("D", "D", "4"))
                .build();
    }

}

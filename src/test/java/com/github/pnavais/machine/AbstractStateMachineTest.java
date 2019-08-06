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

import com.github.pnavais.machine.utils.StateTransitionPrint;
import com.github.pnavais.machine.utils.StateTransitionPrintBuilder;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for state machine tests
 */
public abstract class AbstractStateMachineTest {

    /** The state printer */
    private static StateTransitionPrint statePrinter;

    @BeforeAll
    public static void init() {
        statePrinter = StateTransitionPrintBuilder.newBuilder()
                .stateFormatter(s -> s.getName()+" ["+s.getId()+"]")
                .build();
    }

    /**
     * Retrieves the state printer.
     *
     * @return the state printer
     */
    public static StateTransitionPrint getStatePrinter() {
        return statePrinter;
    }
}

/*
 * Copyright 2017 Pablo Navais
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
package org.payball;


import org.junit.jupiter.api.Test;
import org.payball.machine.StateMachine;
import org.payball.machine.model.StateTransitionMap;
import org.payball.machine.utils.StringUtils;
import org.payball.machine.utils.TransitionPrintBuilder;
import org.payball.machine.utils.TransitionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for simple App.
 */

public class StateMachineTest
{
    /**
     * Tests the initialization of the State Machine
     */
    @Test
    public void testMachineInit() {
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("B").to("C").on("2")
                .selfLoop("C").on("3")
                .from("C").to("D").on("4")
                .build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(4, stateMachine.size(), "State machine size mismatch");
        //assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        //assertEquals(1, stateMachine.getTransitions("A").size(), "Transitions size mismatch");

        TransitionUtils.printTransitions((StateTransitionMap)stateMachine.getTansitionsIndex(), TransitionPrintBuilder.getDefault());
    }

}

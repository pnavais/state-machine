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

package com.github.pnavais.machine.builder;

import com.github.pnavais.machine.AbstractStateMachineTest;
import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.index.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.utils.StateTransitionPrint;
import com.github.pnavais.machine.utils.StateTransitionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for State Machine builder functionality
 */
public class StateMachineBuilderTest extends AbstractStateMachineTest {

    /**
     * Tests the initialization of the State Machine
     */
    @Test
    public void testMachineBuilderInit() {
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("A").to("C").on("2")
                .from("B").to("C").on("3")
                .from("B").to("D").on("4")
                .build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(4, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        assertEquals(2, stateMachine.getTransitions("A").size(), "Transitions size mismatch");
        assertNotNull(stateMachine.getTransitions("B"), "State transitions not retrieved correctly");
        assertEquals(2, stateMachine.getTransitions("B").size(), "Transitions size mismatch");

        StateTransitionUtils.printTransitions((StateTransitionMap) stateMachine.getTransitionsIndex(), getStatePrinter());
    }

    /**
     * Tests the initialization of the State Machine
     */
    @Test
    public void testOverrideTransitionsInit() {
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("A").to("C").on("1")
                .build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(3, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        assertEquals(1, stateMachine.getTransitions("A").size(), "Transitions size mismatch");

        Optional<State> thirdState = stateMachine.find("C");
        assertTrue(thirdState.isPresent(), "Error retrieving state");
        Assertions.assertEquals("C", thirdState.get().getName(), "State name mismatch");

        StateTransitionUtils.printTransitions((StateTransitionMap) stateMachine.getTransitionsIndex(), getStatePrinter());
    }

    @Test
    public void testTransitionMapIdentity() {
        StateMachineBuilder builder = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("A").to("C").on("1");

        StateMachine firstMachine = builder.build();
        StateMachine secondMachine = builder.build();
        assertNotEquals(firstMachine, secondMachine, "State machines must differ");
        assertEquals(firstMachine.getTransitionsIndex(), secondMachine.getTransitionsIndex(), "Transition index mismatch");
    }

    @Test
    public void testTransitionMapModification() {
        StateMachineBuilder builder = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("A").to("C").on("1");

        StateMachine firstMachine = builder.build();
        StateMachine secondMachine = builder.build();

        assertEquals(firstMachine.getTransitionsIndex(), secondMachine.getTransitionsIndex(), "Transition index mismatch");
    }
}

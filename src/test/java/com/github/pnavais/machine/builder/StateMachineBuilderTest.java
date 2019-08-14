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
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for State Machine builder functionality
 */
public class StateMachineBuilderTest extends AbstractStateMachineTest {

    /**
     * Tests the initialization of the State Machine
     */
    @Test
    public void testMachineBuilderInit() {
        StateMachineBuilder stateMachineBuilder = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("A").to("C").on("2")
                .from("B").to("C").on("3")
                .from("B").to("D").on("4");

        StateTransitionMap transitionMap = stateMachineBuilder.getTransitionMap();
        assertNotNull(transitionMap, "Error obtaining transition map");
        StateMachine stateMachine = stateMachineBuilder.build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(4, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        assertEquals(2, stateMachine.getTransitions("A").size(), "Transitions size mismatch");
        assertNotNull(stateMachine.getTransitions("B"), "State transitions not retrieved correctly");
        assertEquals(2, stateMachine.getTransitions("B").size(), "Transitions size mismatch");
        assertEquals(transitionMap, stateMachine.getTransitionsIndex(), "Transition map mismatch");

        getStatePrinterBuilder()
                .printOptions(getStatePrinterOptions())
                .build().printTransitions(stateMachine.getTransitionsIndex());
    }

    /**
     * Tests the initialization of the State Machine
     * with empty messages
     */
    @Test
    public void testMachineBuilderInitWithEmptyMessages() {
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B")
                .from("B").to("A")
                .from(new State("A")).to("C").on("1")
                .from("C").to("D")
                .build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(4, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        assertEquals(2, stateMachine.getTransitions("A").size(), "Transitions size mismatch");

        getStatePrinter().printTransitions(stateMachine.getTransitionsIndex());
        getStatePrinterBuilder().compactMode(true).build().printTransitions(stateMachine.getTransitionsIndex());

        State current = stateMachine.next().getCurrent();
        assertNotNull(current, "Error retrieving next state");
        assertThat(current.getName(), is("B"));

        // Go full circle
        current = stateMachine.next().getCurrent();
        assertNotNull(current, "Error retrieving next state");
        assertThat(current.getName(), is("A"));

        // Break the cycle
        current = stateMachine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving next state");
        assertThat(current.getName(), is("C"));
    }

    /**
     * Tests self loops
     */
    @Test
    public void testMachineBuilderInitWithSelfLoops() {

        AtomicInteger counter = new AtomicInteger();
        FilteredState stateB = new FilteredState(new State("B"));
        stateB.setReceptionHandler((message, state) -> { counter.getAndIncrement(); return Status.PROCEED; });

        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to(stateB)
                .selfLoop(stateB)
                .from("B").to("C").on("2")
                .from("C").to("D")
                .selfLoop("D").build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(4, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("B"), "State transitions not retrieved correctly");
        assertEquals(2, stateMachine.getTransitions("B").size(), "Transitions size mismatch");

        getStatePrinter().printTransitions(stateMachine.getTransitionsIndex());
        getStatePrinterBuilder().compactMode(true).build().printTransitions(stateMachine.getTransitionsIndex());

        State current = stateMachine.next().getCurrent();
        assertNotNull(current, "Error retrieving next state");
        assertThat(current.getName(), is("B"));

        IntStream.range(1,4).forEach(value -> stateMachine.next());
        current = stateMachine.getCurrent();
        assertNotNull(current, "Error retrieving next state");
        assertThat(current.getName(), is("B"));
        assertThat("Error processing self loop", counter.get(), is(4));

        // Break the loop
        current = stateMachine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving next state");
        assertThat(current.getName(), is("C"));
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

        getStatePrinterBuilder()
                .printOptions(getStatePrinterOptions())
                .build().printTransitions(stateMachine.getTransitionsIndex());
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

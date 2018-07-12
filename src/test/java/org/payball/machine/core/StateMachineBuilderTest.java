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

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.payball.machine.StateMachine;
import org.payball.machine.api.transition.TransitionIndex;
import org.payball.machine.api.transition.Transitioner;
import org.payball.machine.builder.StateMachineBuilder;
import org.payball.machine.model.State;
import org.payball.machine.model.StateTransition;
import org.payball.machine.model.StateTransitionMap;
import org.payball.machine.model.StringMessage;
import org.payball.machine.utils.StateTransitionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the creation of State Machines through the Builder
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DisplayName("State Machine Builder Tests")
public class StateMachineBuilderTest extends AbstractStateMachineTest {

    @Test
    @DisplayName("State Machine Builder initialization test")
    void testMachineBuilderInit() {
        StateMachine stateMachine = StateMachineBuilder.newBuilder()
                .from("A").to("B").on("1")
                .and("B").to("C").on("2")
                .build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(3, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        assertEquals(1, stateMachine.getTransitions("A").size(), "Transitions size mismatch");
        assertNotNull(stateMachine.getTransitions("B"), "State transitions not retrieved correctly");
        assertEquals(1, stateMachine.getTransitions("B").size(), "Transitions size mismatch");

        StateTransitionUtils.printTransitions((StateTransitionMap) stateMachine.getTransitionsIndex(), statePrinter);
    }

    @Test
    @DisplayName("Override transitions during build test")
    void testOverrideTransitionsInit() {
        StateMachine stateMachine = StateMachineBuilder.newBuilder()
                .from("A").to("B").on("1")
                .and("A").to("C").on("1")
                .build();

        assertNotNull(stateMachine, "Null state machine found");
        assertEquals(3, stateMachine.size(), "State machine size mismatch");
        assertNotNull(stateMachine.getTransitions("A"), "State transitions not retrieved correctly");
        assertEquals(1, stateMachine.getTransitions("A").size(), "Transitions size mismatch");

        Optional<State> thirdState = stateMachine.find("C");
        assertTrue(thirdState.isPresent(), "Error retrieving state");
        Assertions.assertEquals("C", thirdState.get().getName(), "State name mismatch");

        StateTransitionUtils.printTransitions((StateTransitionMap) stateMachine.getTransitionsIndex(), statePrinter);
    }

    @Test
    @DisplayName("Add a list of transitions during build test")
    void testAddTransitionsFromList() {

        List<StateTransition> transitionList = new ArrayList<>();

        transitionList.add(StateTransition.of("A", "B", "1"));
        transitionList.add(StateTransition.of("B", "C", "2"));

        StateMachineBuilder builder = StateMachineBuilder.newBuilder().addAll(transitionList);
        StateMachine machine = builder.build();

        assertEquals(3, machine.size(), "Error creating machine from transitions list");
        assertEquals(2, machine.getTransitions().size(), "Error retrieving transitions");

        transitionList.add(StateTransition.of("C", "D", "3"));
        builder.from("D").to("D").on("4").addAll(transitionList);

        assertEquals(4, machine.size(), "Error creating machine from transitions list");
        assertEquals(4, machine.getTransitions().size(), "Error retrieving transitions");
    }


    @Test
    @DisplayName("Multiple StateMachines from same Builder test")
    void testTransitionMapIdentity() {
        StateMachineBuilder builder = StateMachineBuilder.newBuilder();
        builder.from("A").to("B").on("1")
                .and("A").to("C").on("2");

        StateMachine firstMachine = builder.build();
        StateMachine secondMachine = builder.build();
        assertNotEquals(firstMachine, secondMachine, "State machines must differ");
        assertEquals(firstMachine.getTransitionsIndex(), secondMachine.getTransitionsIndex(), "Transition index mismatch");
    }

    @Test
    @DisplayName("Aadd all states to State Machine from index test")
    void testTransitionMapCopy() {
        TransitionIndex<State, StateTransition> transitionIndex = StateMachineBuilder.newBuilder()
                .from("A").to("B").on("1")
                .getTransitionIndex();

        StateMachine machineBase = new StateMachine(transitionIndex);
        Transitioner machineExtended = StateMachineBuilder.newBuilder().addAll(transitionIndex.getTransitions())
                .from("B").to("C").on("2").build();

        assertNotNull(machineBase.getTransitions(), "Transitions not created correctly");
        assertEquals(2, machineBase.size(), "Error retrieving machine base size");
        assertEquals(1, machineBase.getTransitions().size(), "Base Transitions not retrieved correctly");

        assertNotNull(machineExtended.getTransitions(), "Transitions not created correctly");
        assertEquals(3, machineExtended.size(), "Error retrieving extended machine size");
        assertEquals(2, machineExtended.getTransitions().size(), "Extended Transitions not retrieved correctly");

        assertEquals(machineBase.size(), machineBase.getTransitionsIndex().size(), "Error retrieving transitions index from base machine");
        assertEquals(machineExtended.size(), machineExtended.getTransitionsIndex().size(), "Error retrieving transitions index from extended machine");
    }

    @Test
    @DisplayName("Extend State Machine and check base not modified test")
    void testTransitionMapCopyIdentity() {
        StateMachine machineBase = StateMachineBuilder.newBuilder()
                .from("A").to("B").on("1")
                .and("B").to("C").on("2")
                .build();

        Transitioner machineExtended = StateMachineBuilder.newBuilder().addAll(machineBase.getTransitions())
                .from("C").to("D").on("3").build();

        machineBase.remove("C");
        assertEquals(2, machineBase.size(), "Error retrieving machine base size");
        assertEquals(1, machineBase.getTransitions().size(), "Base Transitions not retrieved correctly");

        Optional c = machineExtended.find("C");
        assertTrue(c.isPresent(), "Error retrieving state from extended machine");

        assertEquals(4, machineExtended.size(), "Error retrieving machine extended size");
        assertEquals(3, machineExtended.getTransitions().size(), "Extended Transitions not retrieved correctly");
    }

    @Test
    @DisplayName("Modification of State Transitions test")
    void testTransitionMapModification() {
        StateMachineBuilder builder = StateMachineBuilder.newBuilder();
        builder.from("A").to("B").on("1")
                .and(State.named("A")).to("C").on("2");

        StateMachine firstMachine = builder.build();

        Collection<StateTransition> transitions = firstMachine.getTransitions("A");
        assertNotNull(transitions, "Error retrieving the transitions");
        assertEquals(2, transitions.size(), "Transitions size mismatch");
        transitions.remove(StateTransition.of("A", "B", "1"));

        transitions = firstMachine.getTransitions("A");
        assertNotNull(transitions, "Error retrieving the transitions");
        assertEquals(2, transitions.size(), "Transitions size mismatch");

        firstMachine.remove(StateTransition.of("A", "B", "1"));
        transitions = firstMachine.getTransitions("A");
        assertNotNull(transitions, "Error retrieving the transitions");
        assertEquals(1, transitions.size(), "Transitions size mismatch");
    }

    @Test
    @DisplayName("Create State machine using existing transition map")
    void testCreateFromExistingMap() {
        StateMachineBuilder builder = StateMachineBuilder.newBuilder();

        // Create some transitions
        TransitionIndex<State, StateTransition> transitionIndex = builder.from("A").to("B").on("1")
                .selfLoop("A").on("s1")
                .and(State.named("A")).to("C").on("2")
                .from(State.named("C")).to(State.named("D")).on(StringMessage.from("3"))
                .getTransitionIndex();

        // Add additional transitions
        builder.from("D").to("E").on("4")
                .from("F").to("G").on("5")
                .from("G").to("H").on("6")
                .selfLoop(State.named("C")).on("s2")
                .add(StateTransition.of("E", "F", "5"));

        // Check transitions are correct
        StateMachineBuilder otherBuilder = StateMachineBuilder.newBuilder(transitionIndex);
        assertEquals(builder.getTransitionIndex(), otherBuilder.getTransitionIndex(), "Error retrieving transition index");
        otherBuilder.from("B").to("D").on("3");
        builder.selfLoop(State.named("D")).on("4");
        assertEquals(builder.getTransitionIndex(), otherBuilder.getTransitionIndex(), "Error retrieving transition index");
        assertEquals(builder.getTransitionIndex().size(), otherBuilder.getTransitionIndex().size(), "Error adding new states to builder");
        assertEquals(8, builder.getTransitionIndex().size(), "Error adding new states to builder");

        Collection<StateTransition> d = otherBuilder.getTransitionIndex().getTransitions("D");
        assertNotNull(d, "Error retrieving transitions");
        assertEquals(1, d.size(), "Transitions mismatch");
    }
}

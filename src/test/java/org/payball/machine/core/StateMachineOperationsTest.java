/*
 * Copyright 2018 Payball Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.payball.machine.core;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.payball.machine.machine.model.State;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests operations of State Machines.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DisplayName("State Machine Operations Tests")
public class StateMachineOperationsTest extends AbstractStateMachineTest {

    @Test
    @DisplayName("Search existing states test")
    void testStateSearch() {
        Arrays.asList("A", "B", "C", "D").forEach(stateName -> {
            Optional<State> state = defaultMachine.find(stateName);
            assertTrue(state.isPresent(), "Error retrieving state");
            assertEquals(stateName, state.get().getName(), "State names differ");
        });
    }

    @Test
    @DisplayName("Search non existing states test")
    void testStateNotPresentSearch() {
         Optional<State> state = defaultMachine.find("E");
        assertFalse(state.isPresent(), "Error retrieving state");
    }

    @Test
    @DisplayName("Test if the current state is set")
    void testInitialState() {
        State current = defaultMachine.getCurrent();
        assertNotNull(current, "Error retrieving initial state");
        assertEquals("A", current.getName(), "Initial state is not valid");
    }

    @Test
    @DisplayName("Traverse state machine from initial state")
    void testStateWalk() {

        assertNotNull(defaultMachine.getCurrent(), "Error retrieving initial state");
        assertEquals("A", defaultMachine.getCurrent().getName());

        checkStates(Arrays.asList("B", "C", "D"), 0);
    }

    @Test
    @DisplayName("Traverse state machine from another state")
    void testStateWalkFromNode() {

        defaultMachine.setCurrent("B");
        checkStates(Arrays.asList("C", "D"), 1);
    }

    /**
     * Cecks the state traversal from a given starting point.
     *
     * @param stateNames the list of states to traverse
     * @param startIndex the starting index.
     */
    private void checkStates(List<String> stateNames, int startIndex) {

        AtomicInteger counter = new AtomicInteger(startIndex);
        stateNames.forEach(s ->  {
            Optional<State> next = defaultMachine.getNext(String.valueOf(counter.incrementAndGet()));
            assertTrue(next.isPresent(), "Error retrieving next state");
            assertEquals(s, next.get().getName(), "Next State name differs from expected");
            State current = defaultMachine.getCurrent();
            assertEquals(current, next.get(), "Error transitioning to next state");
        });

    }


}

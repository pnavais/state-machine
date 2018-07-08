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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        });
    }

    @Test
    @DisplayName("Search non existing states test")
    void testStateNotPresentSearch() {
         Optional<State> state = defaultMachine.find("E");
        assertFalse(state.isPresent(), "Error retrieving state");
    }

}

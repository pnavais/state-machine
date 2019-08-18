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
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.FilteredState;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
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
                .from("B").to("D").on("4")
                .builder();

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
        stateB.setReceptionHandler(context -> { counter.getAndIncrement(); return Status.PROCEED; });

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
                .from("A").to("C").on("1").builder();

        StateMachine firstMachine = builder.build();
        StateMachine secondMachine = builder.build();
        assertNotEquals(firstMachine, secondMachine, "State machines must differ");
        assertEquals(firstMachine.getTransitionsIndex(), secondMachine.getTransitionsIndex(), "Transition index mismatch");
    }

    @Test
    public void testTransitionMapModification() {
        StateMachineBuilder builder = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("A").to("C").on("1").builder();

        StateMachine firstMachine = builder.build();
        StateMachine secondMachine = builder.build();

        assertEquals(firstMachine.getTransitionsIndex(), secondMachine.getTransitionsIndex(), "Transition index mismatch");
    }

    /**
     * Tests the initialization of the State Machine
     * with filtering capabilities
     */
    @Test
    public void testMachineBuilderWithFiltering() {
        AtomicInteger counter = new AtomicInteger();
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").on("1").leaving(context -> {
                    counter.getAndIncrement();
                    return Status.PROCEED;
                })
                .from("B").to("C").on("2").arriving(context -> {
                    counter.getAndIncrement();
                    return Status.PROCEED;})
        .build();

        getStatePrinter().printTransitions(stateMachine.getTransitionsIndex());
        State current = stateMachine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("B"));
        assertThat("Error executing departure function", counter.get(), is(1));
        current = stateMachine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("C"));
        assertThat("Error executing departure function", counter.get(), is(2));
    }

    /**
     * Tests the initialization of the State Machine
     * with filtering capabilities
     */
    @Test
    public void testMachineBuilderWithFilteringOnEmpty() {
        AtomicInteger counter = new AtomicInteger();
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").leaving(context -> {
                    counter.getAndIncrement();
                    return Status.PROCEED;
                }).arriving(context -> {
                    counter.getAndIncrement();
                    return Status.PROCEED;
                })
                .build();

        getStatePrinter().printTransitions(stateMachine.getTransitionsIndex());
        State current = stateMachine.next().getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("B"));
        assertThat("Error executing departure function", counter.get(), is(2));

        stateMachine.init();
        current = stateMachine.send(Messages.EMPTY).getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("B"));
        assertThat("Error executing departure function", counter.get(), is(4));
    }

    /**
     * Tests the initialization of the State Machine
     * with filtering capabilities
     */
    @Test
    public void testMachineBuilderWithFilteringOverride() {
        AtomicInteger counter = new AtomicInteger();
        FilteredState filteredC = FilteredState.from(new State("C"));
        filteredC.setDispatchHandler(context -> Status.PROCEED);
        StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").on("1").leaving(context -> {
                    counter.getAndIncrement();
                    return Status.PROCEED;
                }).leaving(context -> Status.ABORT)
                .from("B").to(filteredC).on("2").arriving(context -> Status.ABORT)
                .from("C").to("D").arriving(context -> {
                    if (context.getMessage().equals(Messages.EMPTY)) { counter.getAndIncrement(); }
                    return Status.PROCEED; })
                .build();

        getStatePrinter().printTransitions(stateMachine.getTransitionsIndex());
        State current = stateMachine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("A"));
        assertThat("Error executing departure function", counter.get(), is(0));

        stateMachine.setCurrent("B");
        current = stateMachine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("B"));

        stateMachine.setCurrent("C");
        current = stateMachine.next().getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat(current.getName(), is("D"));
        assertThat("Error executing departure function", counter.get(), is(1));
    }

    /**
     * Tests the initialization of the State Machine
     * from a builder using existing transitions
     */
    @Test
    public void testMachineBuilderWithTransition() {
        StateMachine stateMachine = StateMachine.newBuilder()
                .add(new StateTransition("A","1", "B"))
                .add(new StateTransition("A", StringMessage.from("2"), "C"))
                .add(new StateTransition(new State("A"), StringMessage.from("3"), new State("D")))
                .add(new StateTransition("A", StringMessage.from("4"), "E"))
                .add(new StateTransition("A", StringMessage.from("5"), State.from("F").build()))
                .add(new StateTransition("A", StringMessage.from("6"), State.from("G").build()))
                .add(new StateTransition(State.from("A").build(), "7", State.from("H").build()))
                .add(new StateTransition("A", "8", State.from("I").build()))
                .add(new StateTransition(State.from("A").build(), "9", "J"))
                .add(new StateTransition(State.from("A").build(), StringMessage.from("10"), "K"))
                .build();

        assertNotNull(stateMachine, "Error building state machine");
        assertThat("Size mismatch", stateMachine.size(), is(11));

        String[] targets = { "B", "C", "D", "E", "F", "G", "H", "I", "J", "K" };
        IntStream.range(1, 11).forEach(i -> {
            stateMachine.init();
            State current = stateMachine.send(String.valueOf(i)).getCurrent();
            assertNotNull(current, "Error obtaining target state");
            assertThat("Target mismatch", current.getName(), is(targets[i-1]));
        });
    }

    /**
     * Tests the initialization of the State Machine
     * from a builder overriding current states by
     * adding additional filters.
     */
    @Test
    public void testOverrideFilter() {
        AtomicInteger counter = new AtomicInteger();
        FilteredState filteredStateB = new FilteredState(new State("B"));
        filteredStateB.setFinal(true);
        filteredStateB.setReceptionHandler(context -> {
            counter.getAndIncrement();
            return Status.PROCEED;
        });

        StateMachineBuilder stateMachineBuilder = StateMachine.newBuilder()
                .add(new StateTransition("A", "1","B"));

        StateMachine stateMachine = stateMachineBuilder.build();

        stateMachine = stateMachineBuilder
                .add(new StateTransition("A", "2", filteredStateB))
                .build();

        Optional<State> stateB = stateMachine.find("B");
        assertTrue(stateB.isPresent(), "Error obtaining state B");
        assertTrue(stateB.get().isFinal(), "Error obtaining final property");

        State current = stateMachine.send("1").getCurrent();
        assertThat("Error obtaining next target", current.getName(), is("B"));
        assertTrue(current.isFinal(), "Error overriding final property");
        assertThat("Error executing the reception handler", counter.get(), is(1));

        FilteredState newFilteredStateB = FilteredState.from(new State("B"));

        newFilteredStateB.setReceptionHandler(StringMessage.from("3"), context -> {
            counter.getAndDecrement();
            return Status.PROCEED;
        });

        stateMachine = stateMachineBuilder
                .add(new StateTransition("A", "3", newFilteredStateB))
                .build();

        current = stateMachine.send("1").getCurrent();
        assertThat("Error obtaining next target", current.getName(), is("B"));
        assertTrue(current.isFinal(), "Error overriding final property");
        assertThat("Error executing the reception handler", counter.get(), is(1));

        stateMachine.init();
        current = stateMachine.send("3").getCurrent();
        assertThat("Error obtaining next target", current.getName(), is("B"));
        assertTrue(current.isFinal(), "Error overriding final property");
        assertThat("Error executing the reception handler", counter.get(), is(0));

        // Override global reception handler
        FilteredState globalStateB = FilteredState.from(new State("B"));

        globalStateB.setReceptionHandler(context -> {
            counter.set(100);
            return Status.PROCEED;
        });

        stateMachine = stateMachineBuilder
                .add(new StateTransition("A", "4", globalStateB))
                .build();

        stateMachine.init();
        current = stateMachine.send("1").getCurrent();
        assertThat("Error obtaining next target", current.getName(), is("B"));
        assertTrue(current.isFinal(), "Error overriding final property");
        assertThat("Error executing the reception handler", counter.get(), is(100));

    }

    /**
     * Tests the initialization of the State Machine
     * from a builder using global filters.
     */
    @Test
    public void testGlobalFiltering() {

        List<String> messages = new ArrayList<>();

        StateMachineBuilder stateMachineBuilder = StateMachine.newBuilder()
                .add(new StateTransition("A", "1","B"))
                .add(new StateTransition("A", "2","C"))
                .leaving("A").execute(c -> {
                    messages.add(String.format("Departing from [%s] to [%s] on [%s]", c.getSource(), c.getTarget(), c.getMessage()));
                    return Status.PROCEED;
                });

        StateMachine stateMachine = stateMachineBuilder.build();
        State current = stateMachine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error obtaining current state",current.getName(), is("B"));
        assertThat("Error obtaining messages size", messages.size(), is(1));
        assertThat("Messages mismatch", messages.get(0), is("Departing from [A] to [B] on [1]"));

        stateMachine.init();
        messages.clear();
        current = stateMachine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error obtaining current state", current.getName(), is("C"));
        assertThat("Error obtaining messages size", messages.size(), is(1));
        assertThat("Messages mismatch", messages.get(0), is("Departing from [A] to [C] on [2]"));

        // Abort transition on C arrival
        stateMachine = stateMachineBuilder.arriving("C").execute(c -> Status.ABORT).build();
        messages.clear();
        current = stateMachine.send("2").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error obtaining current state", current.getName(), is("A"));
        assertThat("Error obtaining messages size", messages.size(), is(1));
        assertThat("Messages mismatch", messages.get(0), is("Departing from [A] to [C] on [2]"));

        // B Transition still works as expected
        messages.clear();
        current = stateMachine.send("1").getCurrent();
        assertNotNull(current, "Error retrieving current state");
        assertThat("Error obtaining current state", current.getName(), is("B"));
        assertThat("Error obtaining messages size", messages.size(), is(1));
        assertThat("Messages mismatch", messages.get(0), is("Departing from [A] to [B] on [1]"));
    }


    /**
     * Tests the initialization of the State Machine
     * from a builder using global filters.
     */
    @Test
    public void testGlobalFilteringOnEmptyMessages() {
        AtomicInteger counter = new AtomicInteger();

        StateMachineBuilder stateMachineBuilder = StateMachine.newBuilder().
                from("A").to("B").leaving("A").execute(c -> {
                    counter.getAndIncrement();
                    return Status.PROCEED;
                }).
                from("B").to("C")
                .arriving("B").execute(c -> {
                    counter.getAndIncrement();
                    counter.getAndIncrement();
                    return Status.PROCEED;
        });

        StateMachine stateMachine = stateMachineBuilder.build();
        State current = stateMachine.next().getCurrent();
        assertThat("Error obtaining current state", current.getName(), is("B"));
        assertThat("Error executing arriving and leaving handlers", counter.get(), is(3));

        stateMachineBuilder.from("C").to("D").leaving(new State("C")).execute(c -> {
                    counter.getAndDecrement();
                    return Status.PROCEED;
                }).from("D").to("E").arriving(new State("D")).execute(c -> {
                    counter.getAndDecrement();
                    counter.getAndDecrement();
                    return Status.PROCEED;
        });

        stateMachine = stateMachineBuilder.build();
        stateMachine.setCurrent("C");
        current = stateMachine.next().next().getCurrent();
        assertThat("Error obtaining current state", current.getName(), is("E"));
        assertThat("Error executing arriving and leaving handlers", counter.get(), is(0));
    }
}

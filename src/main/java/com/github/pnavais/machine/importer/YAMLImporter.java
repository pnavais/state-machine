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

package com.github.pnavais.machine.importer;

import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.api.exception.FileImportException;
import com.github.pnavais.machine.api.exception.YAMLParseException;
import com.github.pnavais.machine.api.importer.Importer;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.builder.StateMachineBuilder;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import lombok.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An importer allowing to parse a given state machine YAML representation
 * (States and transitions) into an actual State machine.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YAMLImporter implements Importer<String, State, Message, StateMachine> {

    /** The regex to match starting whitespaces */
    private static final Pattern marginRegex = Pattern.compile("(^[\\s]*)");

    /** The state machine builder */
    @Builder.Default
    private StateMachineBuilder builder = StateMachine.newBuilder();

    /** The current state */
    private State currentState;

    /** The File system */
    @Builder.Default
    @NonNull
    @Getter
    @Setter
    private FileSystem fileSystem = FileSystems.getDefault();

    /**
     * Parses the given YAML String containing
     * the State machine specification or throws
     * YAMLImportException in case of errors.
     *
     * @param input the input format
     * @return the built state machine
     */
    @Override
    public StateMachine parse(String input) {
        // Split input  in multiple lines
        String separator = Pattern.quote(System.lineSeparator());
        return processInput(Arrays.asList(input.split(separator)));
    }

    /**
     * Processes the given input file to obtain
     * an actual {@link StateMachine} instance.
     *
     * @param inputFile the input file
     * @return the build state machine
     */
    public StateMachine parseFile(String inputFile) {
        return parseFile(getFileSystem().getPath(inputFile));
    }

    /**
     * Processes the given input file to obtain
     * an actual {@link StateMachine} instance.
     *
     * @param inputFile the input file
     * @return the build state machine
     */
    @Override
    public StateMachine parseFile(Path inputFile) {

        StateMachine machine;

        // Process the input file lines
        try (Stream<String> stream = Files.lines(inputFile, StandardCharsets.UTF_8)) {
            List<String> lines = new ArrayList<>();
            stream.forEachOrdered(lines::add);
            machine = processInput(lines);
        } catch (IOException e) {
            throw new FileImportException("Error processing ["+inputFile+"]", e);
        }

        return machine;
    }


    /**
     * Process the given input lines containing
     * the YAML representation.
     *
     * @param lines the YAML representation lines
     * @return the built state machine
     */
    private StateMachine processInput(List<String> lines) {

        Map<String, State> states = new LinkedHashMap<>();
        Collection<StateTransition> transitions = new ArrayList<>();

        // Read all states and transitions until exhaustion
        for (ListIterator<String> i = lines.listIterator(); i.hasNext(); ) {
            String line = i.next();
            if (line.matches("^[\\s]*states:")) {
                readAllStates(i, states);
            } else if (line.matches("^[\\s]*transitions:")) {
                readAllTransitions(i, transitions);
            }
        }

        return buildStateMachine(states, transitions);
    }

    /**
     * Read all subsequent states until end of current state block
     *
     * @param iterator the list iterator
     * @param states the states list
     */
    private void readAllStates(ListIterator<String> iterator, Map<String, State> states) {

        String previous = iterator.previous();
        int previousMargin = getLineMargin(previous);
        iterator.next();

        // Loop for states until transitions found or no more lines
        while (iterator.hasNext()) {
            String line = iterator.next();

            int currentMargin = getLineMargin(line);

            // Break the loop if margin is lower
            if (currentMargin < previousMargin) {
                iterator.previous();
                break;
            }

            // Process an entire state
            State state = processState(iterator).orElseThrow(() -> new YAMLParseException("Error processing state at line ["+iterator.previousIndex()+1+"] : "+line));
            states.put(state.getName(), state);

            previousMargin = currentMargin;
        }
    }

    /**
     * Read all subsequent states until end of current state block
     *
     * @param iterator the list iterator
     * @param transitions the state transitions
     */
    private void readAllTransitions(ListIterator<String> iterator, Collection<StateTransition> transitions) {

        // Get current section margin
        String previous = iterator.previous();
        int previousMargin = getLineMargin(previous);
        iterator.next();

        while (iterator.hasNext()) {
            String line = iterator.next();

            int currentMargin = getLineMargin(line);

            // Break the loop if margin is lower
            if (currentMargin < previousMargin) {
                iterator.previous();
                break;
            }

            // Process an entire state transition
            StateTransition transition = processStateTransition(iterator)
                    .orElseThrow(() -> new YAMLParseException("Error processing state at line ["+iterator.previousIndex()+1+"] : "+line));
            transitions.add(transition);

            previousMargin = currentMargin;
        }
    }

    /**
     * Builds the State machine using the given states and transition relations.
     *
     * @param states the map containing the states
     * @param transitions the transitions
     * @return the built state machine
     */
    private StateMachine buildStateMachine(Map<String, State> states, Collection<StateTransition> transitions) {

        // Check that at least one transition is defined
        if (transitions.isEmpty()) {
            throw new YAMLParseException("No transitions found");
        }

        transitions.forEach(transition -> {
            // For each State in the transition , merge the information with the actual state
            transition.getOrigin().merge(states.get(transition.getOrigin().getName()));
            transition.getTarget().merge(states.get(transition.getTarget().getName()));

            // Add it to the builder
            builder.add(transition);
        });

        StateMachine machine = builder.build();
        if (currentState != null) {
            machine.setCurrent(currentState);
        }

        return machine;
    }

    /**
     * Process the lines to find a complete state
     *
     * @param iterator the iterator
     * @return the built State
     */
    private Optional<State> processState(ListIterator<String> iterator) {

        // Creates an aggregator to process State input lines
        StateAggregator stateAggregator = new StateAggregator();

        // Retrieve current section margin
        String previous = iterator.previous();
        int sectionMargin = getLineMargin(previous);
        int previousMargin = sectionMargin;
        iterator.next();

        while (iterator.hasNext()) {
            String line = iterator.next();
            int currentMargin = getLineMargin(line);

            // Exit if new state or change of section
            if (line.matches("^[\\s]+- state:") || currentMargin <= sectionMargin) {
                iterator.previous();
                break;
            }

            // Check indentation
            checkMarginOutOfBounds(previousMargin, line, iterator.previousIndex()+1);

            stateAggregator.process(line);
            previousMargin = currentMargin;
        }

        // Updates the current state if found
        Optional<State> state = stateAggregator.getState();
        currentState = stateAggregator.hasCurrentState() ? state.orElse(currentState) : currentState;

        return stateAggregator.getState();
    }

    /**
     * Process the lines to find a complete state transition
     *
     * @param iterator the iterator
     * @return the built State transition
     */
    private Optional<StateTransition> processStateTransition(ListIterator<String> iterator) {

        StateTransitionAggregator stateTransitionAggregator = new StateTransitionAggregator();

        // Retrieve current section margin
        String previous = iterator.previous();
        int sectionMargin = getLineMargin(previous);
        int previousMargin = sectionMargin;
        iterator.next();

        while (iterator.hasNext()) {
            String line = iterator.next();
            int currentMargin = getLineMargin(line);

            // Exit on change of indentation , new transition is found
            if (line.matches("^[\\s]+- transition:") || currentMargin <= sectionMargin) {
                iterator.previous();
                break;
            }

            // Check indentation
            checkMarginOutOfBounds(previousMargin, line, iterator.previousIndex()+1);

            stateTransitionAggregator.process(line);
            previousMargin = currentMargin;
        }

        return stateTransitionAggregator.getTransition();
    }

    /**
     * Parses a property line returning the key/pair
     * values.
     *
     * @param line the property line
     * @return the key/pair result
     */
    private static String[] parseProperty(String line) {
        String[] result = { "", "" };
        String[] split = line.split(":");
        result[0] = split[0].trim();
        if (split.length == 2) {
            result[1] = split[1].trim().replaceAll("^\"", "").replaceAll("\"$", "");
        }
        return result;
    }

    /**
     * Computes the margin size of a given line i.e.
     * the number of starting whitespace characters.
     *
     * @param line the line to check
     * @return the margin size.
     */
    private static int getLineMargin(String line) {
        int marginSize = 0;

        Matcher matcher = marginRegex.matcher(line);
        if (matcher.find()) {
            marginSize = matcher.group(1).length();
        }

        return marginSize;
    }

    /**
     * Checks if the current line respects the margin limit
     * imposed by the given margin.
     *
     * @param minMargin the current margin
     * @param line the line to check
     * @param idx the current index
     */
    private static void checkMarginOutOfBounds(int minMargin, String line, int idx) {
        if (getLineMargin(line) < minMargin) {
            throw new YAMLParseException("Error processing line ["+idx+"].Wrong indentation found for : "+line);
        }
    }

    /**
     * Process a given State entry in string format
     */
    private static class StateAggregator {

        /** Flag to control if properties follows */
        private boolean addProps;

        /** Flag to check if this a current state */
        private boolean current;

        private State.StateBuilder stateBuilder;

        /**
         * The constructor
         */
        StateAggregator() {
             stateBuilder = new State.StateBuilder();
        }

        /**
         * Process an State line keeping the information
         * in the builder and registering the appropriate flags.
         *
         * @param stateLine the line to be processed
         */
        void process(String stateLine) {
            // Parse name, final status and properties map
            String[] prop = parseProperty(stateLine);
            if (prop[0].equals("name")) {
                stateBuilder.named(prop[1]);
            } else if (prop[0].equals("final") && (prop[1].equals("true"))) {
                stateBuilder.isFinal(true);
            } else if (prop[0].equals("current")) {
                current = true;
            } else if (prop[0].equals("properties")) {
                addProps = true;
            } else if (addProps) {
                stateBuilder.property(prop[0], prop[1]);
            }
        }

        /**
         * Builds and retrieves the State with the currently
         * stored state information.
         *
         * @return the state or null if not possible to build
         */
        Optional<State> getState() {
            return Optional.ofNullable(stateBuilder.build());
        }

        /**
         * Checks whether the state currently stored
         * in the builder is final or not.
         *
         * @return true if state is final an valid, false otherwise
         */
        boolean hasCurrentState() {
            return current && getState().isPresent();
        }
    }


    /**
     * Process a given State Transition entry in string format
     */
    private static class StateTransitionAggregator {

        /** The source of the transition */
        private State origin;

        /** The target of the transition */
        private State target;

        /** The message */
        private Message message;

        /**
         * Default constructor
         */
        StateTransitionAggregator() {
             message = Messages.EMPTY;
        }

        /**
         * Process an State Transition line keeping the information
         * in the class and registering the appropriate flags.
         *
         * @param transitionLine the line to be processed
         */
        void process(String transitionLine) {
            // Parse origin, target and message
            String[] prop = parseProperty(transitionLine);
            if (prop[0].equals("source")) {
                origin = new State(prop[1]);
            } else if (prop[0].equals("target")) {
                target = new State(prop[1]);
            } else if (prop[0].equals("any") && (prop[1].equals("true"))) {
                message = Messages.ANY;
            } else if (prop[0].equals("message")) {
                message = StringMessage.from(prop[1]);
            }
        }

        /**
         * Creates the state transition from the information
         * currently stored.
         *
         * @return the transition or null if not possible to build
         */
        Optional<StateTransition> getTransition() {
            Optional<StateTransition> transition = Optional.empty();

            if ((origin != null) && (target != null)) {
                transition = Optional.of(new StateTransition(origin, message, target));
            }
            return transition;
        }

    }
}

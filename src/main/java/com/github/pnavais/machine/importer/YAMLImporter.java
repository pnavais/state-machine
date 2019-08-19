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
import com.github.pnavais.machine.builder.StateMachineBuilder;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
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
        return parseFile(fileSystem.getPath(inputFile));
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

        for (ListIterator<String> i = lines.listIterator(); i.hasNext(); ) {
            String line = i.next();
            if (line.matches("^[\\s]*states:")) {
                // Read all states until exhaustion
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

        // Get current section margin
        String previous = iterator.previous();
        int sectionMargin = getLineMargin(previous);
        iterator.next();

        // Loop for states until transitions found or no more lines
        while (iterator.hasNext()) {
            String line = iterator.next();

            // Break the loop if transitions found
            if (line.matches("^[\\s]*transitions:")) {
                iterator.previous();
                break;
            }

            // Check margin restriction
            checkMarginOutOfBounds(sectionMargin, line, iterator.previousIndex()+1);

            // Process an entire state
            State state = processState(iterator).orElseThrow(() -> new YAMLParseException("Error processing state at line ["+iterator.previousIndex()+1+"]"));
            states.put(state.getName(), state);
        }
    }

    /**
     * Read all subsequent states until end of current state block
     *
     * @param iterator the list iterator
     * @param transitions the state transitions
     */
    private void readAllTransitions(ListIterator<String> iterator, Collection<StateTransition> transitions) {
        while (iterator.hasNext()) {
            String line = iterator.next();
            System.out.println("Transition >> ["+line+"]");
            if (line.matches("^[\\s]*states:")) {
                iterator.previous();
                break;
            }
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
        transitions.forEach(transition -> {
            // For each State in the transition , merge the information with the actual state
            transition.getOrigin().merge(states.get(transition.getOrigin().getName()));
            transition.getTarget().merge(states.get(transition.getTarget().getName()));

            // Add it to the builder
            builder.add(transition);
        });

        return builder.build();
    }

    /**
     * Process the lines to find a complete state
     *
     * @param iterator the iterator
     * @return the built State
     */
    private Optional<State> processState(ListIterator<String> iterator) {

        Optional<State> state = Optional.empty();
        State.StateBuilder stateBuilder = new State.StateBuilder();

        String previous = iterator.previous();
        int sectionMargin = getLineMargin(previous);
        iterator.next();

        while (iterator.hasNext()) {
            String line = iterator.next();

            // Exit on change of indentation or new state is found
            if (line.matches("^[\\s]+- state:") || getLineMargin(line)<=sectionMargin) {
                iterator.previous();
                state = Optional.ofNullable(stateBuilder.build());;
                break;
            }

            if (line.matches("^[\\s]+name:[\\s]*\".+\"")) {
                System.out.println("NAME");
                stateBuilder.named(line);
            }

            System.out.println("STATE >> ["+line+"]");
        }

        return state;
    }



    /**
     * Computes the margin size of a given line i.e.
     * the number of starting whitespace characters.
     *
     * @param line the line to check
     * @return the margin size.
     */
    private int getLineMargin(String line) {
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
    private void checkMarginOutOfBounds(int minMargin, String line, int idx) {
        if (getLineMargin(line) <= minMargin) {
            throw new YAMLParseException("Error processing line ["+idx+"].Wrong indentation found for : "+line);
        }
    }
}

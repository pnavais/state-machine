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

import com.github.pnavais.machine.AbstractStateMachineTest;
import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.api.exception.FileImportException;
import com.github.pnavais.machine.api.exception.YAMLParseException;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains the unit tests for the YAML Importer
 */
public class YAMLImporterTest extends AbstractStateMachineTest {

    /**
     * The test filesystem
     */
    private FileSystem testFS = Jimfs.newFileSystem(Configuration.unix());

    @Test
    public void testStateMachineParsing() {
        String inputStates = getInputStates();
        String inputTransitions = getInputTransitions();

        validateStateMachine(inputStates + NL + inputTransitions);
        validateStateMachine(inputTransitions + NL + inputStates);
    }

    @Test
    public void testOnlyTransitionsParsing() {
        String input = "transitions:" + NL +
                "  - transition:" + NL +
                "      source: \"A\"" + NL +
                "      target: \"B\"";
        StateMachine stateMachine = YAMLImporter.builder().build().parse(input);
        assertNotNull(stateMachine, "Error parsing the state machine");
        assertThat(stateMachine.size(), is(2));
        assertTrue(stateMachine.getTransitionsIndex().contains(new StateTransition("A", Messages.EMPTY, "B")));
    }

    @Test
    public void testStateMachineWrongParsing() {

        // Wrong state created
        String input = "states:" + NL +
                "  - state:" + NL +
                "name: \"A\"" + NL;
        testWrongInput(input);

        // State Missing name
        input = "states:" + NL +
                "  - state:" + NL +
                "      current: \"true\"" + NL;
        testWrongInput(input);

        // Wrong indentation
        input = "states:" + NL +
                "  - state:" + NL +
                "     name: \"A\"" + NL +
                "    current: \"true\"" + NL;
        testWrongInput(input);

        // No transitions found
        input = "states:" + NL +
                "  - state:" + NL +
                "     name: \"A\"" + NL;
        testWrongInput(input);

        // Wrong transition specified
        String transitionInput = "states:" + NL +
                "  - state:" + NL +
                "     name: \"A\"" + NL +
                "transitions:" + NL +
                "  - transition:" + NL;

        String source = "      source: \"A\"" + NL;
        String target = "      target: \"B\"" + NL;
        String message = "      message: \"2\"" + NL;

        testWrongInput(transitionInput + source);
        testWrongInput(transitionInput + source + message);
        testWrongInput(transitionInput + target);
        testWrongInput(transitionInput + target + message);
    }

    @Test
    public void testStateMachineParsingFromFile() {
        YAMLImporter yamlImporter = new YAMLImporter();

        yamlImporter.setFileSystem(testFS);

        Path inputPath = testFS.getPath("input.yml");

        try {
            Files.write(inputPath, ImmutableList.of(getInputStates()), StandardCharsets.UTF_8);
            Files.write(inputPath, ImmutableList.of(getInputTransitions()), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            fail("Error creating test file");
        }

        StateMachine stateMachine = yamlImporter.parseFile(inputPath);
        validateStateMachine(stateMachine);

        stateMachine = yamlImporter.parseFile("input.yml");
        validateStateMachine(stateMachine);
    }

    @Test
    public void testStateMachineWrongParsingFromFile() {
        try {
            YAMLImporter.builder().fileSystem(testFS).build().parseFile("input2.yml");
            fail("Exception not raised");
        } catch (Exception e) {
            assertTrue(e instanceof FileImportException);
        }
    }

    /**
     * Validates the state machine created with a given YAML input
     * specification.
     *
     * @param input the YAML Input specification
     */
    private void validateStateMachine(String input) {
        StateMachine stateMachine = YAMLImporter.builder().build().parse(input);
        validateStateMachine(stateMachine);
    }

    /**
     * Validates the state machine  with respect to
     * test predefined expectations
     *
     * @param stateMachine the state machine
     */
    private void validateStateMachine(StateMachine stateMachine) {
        assertNotNull(stateMachine, "Error building state machine");
        getStatePrinterBuilder().compactMode(true).title("Reconstructed from YAML").build().printTransitions(stateMachine.getTransitionsIndex());

        assertThat("State machine size mismatch", stateMachine.size(), is(4));
        assertThat("State transitions size mismatch", stateMachine.getAllTransitions().size(), is(3));
        assertTrue(stateMachine.getTransitionsIndex().contains(new StateTransition("A", Messages.ANY, "B")));
        assertTrue(stateMachine.getTransitionsIndex().contains(new StateTransition("B", "2", "D")));
        assertTrue(stateMachine.getTransitionsIndex().contains(new StateTransition("B", Messages.EMPTY, "C")));
        boolean[] finalStates = {false, false, true, true};
        final int[] i = {0};
        Arrays.asList("A", "B", "C", "D").forEach(s -> {
            Optional<State> state = stateMachine.find(s);
            assertTrue(state.isPresent());
            assertEquals(finalStates[i[0]], state.get().isFinal());
            i[0]++;
        });

        assertNotNull(stateMachine.getCurrent(), "Error building state machine");
        assertThat("Error building state machine", stateMachine.getCurrent().getName(), is("B"));
    }

    /**
     * Expects an exception parsing wrong YAML input
     * for the State Machine specification.
     *
     * @param input the input
     */
    private void testWrongInput(String input) {
        assertThrows(YAMLParseException.class, () -> {
            YAMLImporter.builder().build().parse(input);
        }, "Error obtaining exception");
    }

    /**
     * Retrieves the YAML String representation
     * of the States under test.
     */
    private String getInputStates() {
        return "states:" + NL +
                "  - state:" + NL +
                "      name: \"A\"" + NL +
                "  - state:" + NL +
                "      name: \"B\"" + NL +
                "      current: \"true\"" + NL +
                "      properties:" + NL +
                "        color: \"#1122DD\"" + NL +
                "  - state:" + NL +
                "      name: \"D\"" + NL +
                "      final: \"true\"" + NL +
                "      properties:" + NL +
                "        style: \"filled\"" + NL +
                "        fillcolor: \"#C2B3FF\"" + NL +
                "  - state:" + NL +
                "      name: \"C\"" + NL +
                "      final: \"true\"" + NL +
                "      properties:" + NL +
                "        style: \"filled\"" + NL +
                "        fillcolor: \"#C2B3FF\"";
    }

    /**
     * Retrieves the YAML String representation
     * of the Transitions under test.
     */
    private String getInputTransitions() {
        return "transitions:" + NL +
                "  - transition:" + NL +
                "      source: \"A\"" + NL +
                "      target: \"B\"" + NL +
                "      any: \"true\"" + NL +
                "  - transition:" + NL +
                "      source: \"B\"" + NL +
                "      target: \"D\"" + NL +
                "      message: \"2\"" + NL +
                "  - transition:" + NL +
                "      source: \"B\"" + NL +
                "      target: \"C\"";
    }

}

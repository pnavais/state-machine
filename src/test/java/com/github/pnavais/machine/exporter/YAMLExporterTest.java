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

package com.github.pnavais.machine.exporter;

import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.api.exception.FileExportException;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.model.State;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains the unit tests for the YAML Exporter
 */
public class YAMLExporterTest extends AbstractExporterTest {

    @Override
    protected String getOutputDirectory() {
        return testFS.getSeparator() + "yml_files" + testFS.getSeparator();
    }

    /**
     * Exports a simple state machine
     * and checks the output.
     */
    @Test
    public void testSimpleStateMachineExport() {

        String expected = "states:" + NL +
                "    - state:" + NL +
                "            name: \"A\"" + NL +
                "    - state:" + NL +
                "            name: \"B\"" + NL +
                "    - state:" + NL +
                "            name: \"C\"" + NL +
                "transitions:" + NL +
                "    - transition:" + NL +
                "            source: \"A\"" + NL +
                "            target: \"B\"" + NL +
                "            message: \"1\"" + NL +
                "    - transition:" + NL +
                "            source: \"B\"" + NL +
                "            target: \"C\"" + NL +
                "            message: \"2\"";

        StateMachine machine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("B").to("C").on("2").build();

        String exported = YAMLExporter.builder().build().export(machine);
        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a state machine with custom messages
     * and checks the output.
     */
    @Test
    public void testStateMachineWithMessagesExport() {

        String expected = "states:" + NL +
                "    - state:" + NL +
                "            name: \"A\"" + NL +
                "    - state:" + NL +
                "            name: \"B\"" + NL +
                "    - state:" + NL +
                "            name: \"C\"" + NL +
                "    - state:" + NL +
                "            name: \"D\"" + NL +
                "    - state:" + NL +
                "            name: \"E\"" + NL +
                "transitions:" + NL +
                "    - transition:" + NL +
                "            source: \"A\"" + NL +
                "            target: \"B\"" + NL +
                "            message: \"1\"" + NL +
                "    - transition:" + NL +
                "            source: \"B\"" + NL +
                "            target: \"C\"" + NL +
                "            message: \"2\"" + NL +
                "    - transition:" + NL +
                "            source: \"B\"" + NL +
                "            target: \"D\"" + NL +
                "    - transition:" + NL +
                "            source: \"B\"" + NL +
                "            target: \"E\"" + NL +
                "            any: \"true\"" + NL +
                "    - transition:" + NL +
                "            source: \"C\"" + NL +
                "            target: \"C\"";

        StateMachine machine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("B").to("C").on("2")
                .from("B").to("D")
                .from("B").to("E").on(Messages.ANY)
                .selfLoop("C").build();

        String exported = YAMLExporter.builder().build().export(machine);

        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with default options and checks the output.
     */
    @Test
    public void testStateMachineWithPropertiesExport() {

        String expected = "states:" + NL +
                "    - state:" + NL +
                "            name: \"A\"" + NL +
                "            properties:" + NL +
                "                shape: \"box\"" + NL +
                "    - state:" + NL +
                "            name: \"B\"" + NL +
                "    - state:" + NL +
                "            name: \"C\"" + NL +
                "            final: \"true\"" + NL +
                "transitions:" + NL +
                "    - transition:" + NL +
                "            source: \"A\"" + NL +
                "            target: \"B\"" + NL +
                "            message: \"1\"" + NL +
                "    - transition:" + NL +
                "            source: \"B\"" + NL +
                "            target: \"C\"" + NL +
                "            message: \"2\"";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").property("shape", "box").build()).to("B").on("1")
                .from("B").to(State.from("C").isFinal(true).build()).on("2").build();

        String exported = YAMLExporter.builder().build().export(machine);

        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with custom options and check the output
     */
    @Test
    public void testStateMachineWithCustomPropertiesExport() {

        String expected = "states:" + NL +
                "  - state:" + NL +
                "      name: \"A\"" + NL +
                "      current: \"true\"" + NL +
                "      properties:" + NL +
                "        shape: \"box\"" + NL +
                "  - state:" + NL +
                "      name: \"B\"" + NL +
                "  - state:" + NL +
                "      name: \"C\"" + NL +
                "      final: \"true\"" + NL +
                "transitions:" + NL +
                "  - transition:" + NL +
                "      source: \"A\"" + NL +
                "      target: \"B\"" + NL +
                "      message: \"1\"" + NL +
                "  - transition:" + NL +
                "      source: \"B\"" + NL +
                "      target: \"C\"" + NL +
                "      message: \"2\"";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").property("shape", "box").build()).to("B").on("1")
                .from("B").to(State.from("C").isFinal(true).build()).on("2").build();

        String exported = YAMLExporter.builder()
                .showCurrent(true)
                .graphName("TestGraph")
                .marginSize(YAMLExporter.MarginSize.TWO_SP)
                .build().export(machine);

        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with custom options and check the output
     */
    @Test
    public void testStateMachineManualExport() {
        String expected = "states:" + NL +
                "  - state:" + NL +
                "      name: \"A\"" + NL +
                "  - state:" + NL +
                "      name: \"B\"" + NL +
                "  - state:" + NL +
                "      name: \"C\"" + NL +
                "      final: \"true\"" + NL +
                "transitions:" + NL +
                "  - transition:" + NL +
                "      source: \"A\"" + NL +
                "      target: \"B\"" + NL +
                "      message: \"1\"" + NL +
                "  - transition:" + NL +
                "      source: \"A\"" + NL +
                "      target: \"C\"" + NL +
                "      message: \"2\"";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").build()).to("B").on("1")
                .from("A").to(State.from("C").isFinal(true).build()).on("2").build();

        YAMLExporter yamlExporter = new YAMLExporter();
        yamlExporter.setMarginSize(YAMLExporter.MarginSize.TWO_SP);
        String exported = yamlExporter.export(machine);

        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with custom options to and output file
     */
    @Test
    public void testStateMachineFileExport() {

        String expected = "states:" + NL +
                "    - state:" + NL +
                "            name: \"A\"" + NL +
                "    - state:" + NL +
                "            name: \"B\"" + NL +
                "transitions:" + NL +
                "    - transition:" + NL +
                "            source: \"A\"" + NL +
                "            target: \"B\"" + NL +
                "            message: \"1\"";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").build()).to("B").on("1").build();

        Path outputPath = testFS.getPath(getOutputDirectory()+"output.yml");

        YAMLExporter exporter = YAMLExporter.builder().fileSystem(testFS).build();
        exporter.exportToFile(machine, outputPath);
        exporter.exportToFile(machine, getOutputDirectory()+"output2.yml");
        String exported = exporter.export(machine);

        try {
            String outputTestFile = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
            String outputTestFile2 = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
            assertThat("Error exporting to file", outputTestFile, is(expected));
            assertThat("Error exporting to file", outputTestFile, is(exported));
            assertThat("Error exporting to file", outputTestFile2, is(exported));
        } catch (IOException e) {
            fail("Error reading output file");
        }
    }

    /**
     * Tests the exception when export failed due to
     * I/O issues.
     */
    @Test
    public void testStateMachineFileExportFailure() {
        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").build()).to("B").on("1").build();

        Path outputPath = testFS.getPath("/tmp2/output.yml");

        try {
            YAMLExporter.builder().build().exportToFile(machine, outputPath);
            fail("Error testing export failure");
        } catch (Exception e) {
            assertEquals(e.getClass(), FileExportException.class, "Exception mismatch");
        }
    }

    /**
     * Tests the exception when export failed due to
     * wrong output file.
     */
    @Test
    public void testStateMachineFileExportToDirFailure() {
        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").build()).to("B").on("1").build();

        Path outputPath = testFS.getPath(getOutputDirectory());

        try {
            YAMLExporter.builder().build().exportToFile(machine, outputPath);
            fail("Error testing export failure");
        } catch (Exception e) {
            assertEquals(e.getClass(), FileExportException.class, "Exception mismatch");
        }
    }

}

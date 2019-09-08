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
import com.github.pnavais.machine.exporter.util.ColorTranslator;
import com.github.pnavais.machine.model.State;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains the unit tests for the DOT Exporter
 */
public class DOTExporterTest extends AbstractExporterTest {

    @Override
    protected String getOutputDirectory() {
        return testFS.getSeparator() + "dot_files" + testFS.getSeparator();
    }

    /**
     * Exports a simple state machine
     * and checks the output.
     */
    @Test
    public void testSimpleStateMachineExport() {

        String expected = "digraph G {" + NL +
                TB + "rankdir=\"LR\";" + NL +
                TB + "A -> B [label=\"1\"];" + NL +
                TB + "B -> C [label=\"2\"];" + NL +
                "}";

        StateMachine machine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("B").to("C").on("2").build();

        String exported = DOTExporter.builder().build().export(machine);
        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a state machine with custom messages
     * and checks the output.
     */
    @Test
    public void testStateMachineWithMessagesExport() {

        String expected = "digraph G {" + NL +
                TB + "rankdir=\"LR\";" + NL +
                TB + "A -> B [label=\"1\"];" + NL +
                TB + "B -> C [label=\"2\"];" + NL +
                TB + "B -> D" + NL +
                TB + "B -> E [label=\"*\"];" + NL +
                TB + "C -> C" + NL +
                "}";

        StateMachine machine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("B").to("C").on("2")
                .from("B").to("D")
                .from("B").to("E").on(Messages.ANY)
                .selfLoop("C").build();

        String exported = DOTExporter.builder().build().export(machine);
        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with default options and checks the output.
     */
    @Test
    public void testStateMachineWithPropertiesExport() {
        
        String expected = "digraph G {" + NL +
                TB + "rankdir=\"LR\";" + NL +
                TB + "A [shape=\"box\"];" + NL +
                TB + "C [style=\"filled\", fillcolor=\""+DOTExporter.DEFAULT_FINAL_COLOR+"\", final=\"true\"];" + NL +
                TB + "A -> B [label=\"1\"];" + NL +
                TB + "B -> C [label=\"2\"];" + NL +
                "}";
        
        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").property("shape", "box").build()).to("B").on("1")
                .from("B").to(State.from("C").isFinal(true).build()).on("2").build();

        String exported = DOTExporter.builder().build().export(machine);
        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with custom options and check the output
     */
    @Test
    public void testStateMachineWithCustomPropertiesExport() {

        Color color = Color.decode("#FF22FF");

        String expected = "digraph TestGraph {" + NL
                + TB + "rankdir=\"TB\";" + NL
                + TB + "A [color=\"0.8333, 0.8667, 1.0000\", current=\"true\", shape=\"box\"];" + NL
                + TB + "C [style=\"filled\", fillcolor=\"0.8333, 0.8667, 1.0000\", final=\"true\"];" + NL
                + TB + "A -> B [label=\"1\"];" + NL
                + TB + "B -> C [label=\"2\"];" + NL
                + "}";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").property("shape", "box").build()).to("B").on("1")
                .from("B").to(State.from("C").isFinal(true).build()).on("2").build();

        String exported = DOTExporter.builder()
                .useHSB(true)
                .showCurrent(true)
                .graphName("TestGraph")
                .finalStateColor(color)
                .currentStateColor(color)
                .rankDir(DOTExporter.RankDir.TB)
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

        Color color = Color.decode("#FFFFFF");

        String expected = "digraph G {" + NL +
                TB + "rankdir=\"LR\";" + NL +
                TB + "C [style=\"filled\", fillcolor=\""+ ColorTranslator.toHSBColor(color) +"\", final=\"true\"];" + NL +
                TB + "A -> B [label=\"1\"];" + NL +
                TB + "A -> C [label=\"2\"];" + NL +
                "}";


        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").build()).to("B").on("1")
                .from("A").to(State.from("C").isFinal(true).build()).on("2").build();

        DOTExporter dotExporter = new DOTExporter();
        dotExporter.setUseHSB(true);
        dotExporter.setFinalStateColor(color);
        String exported = dotExporter.export(machine);

        assertNotNull(exported, "Error retrieving exported state machine contents");
        assertThat("Error comparing exported output", exported, is(expected));
    }

    /**
     * Exports a simple state machine
     * with custom options to and output file
     */
    @Test
    public void testStateMachineFileExport() {
        
        String expected = "digraph G {" + NL +
                TB + "rankdir=\"LR\";" + NL +
                TB + "A -> B [label=\"1\"];" + NL +
                "}";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("A").build()).to("B").on("1").build();

        Path outputPath = testFS.getPath(getOutputDirectory()+"output.gv");

        DOTExporter exporter = DOTExporter.builder().fileSystem(testFS).build();
        exporter.exportToFile(machine, outputPath);
        exporter.exportToFile(machine, getOutputDirectory()+"output2.gv");
        String exported = exporter.export(machine);

        try {
            String outputTestFile = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
            String outputTestFile2 = new String(Files.readAllBytes(outputPath), StandardCharsets.UTF_8);
            assertThat("Error exporting to file", expected, is(outputTestFile));
            assertThat("Error exporting to file", exported, is(outputTestFile));
            assertThat("Error exporting to file", exported, is(outputTestFile2));
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

        Path outputPath = testFS.getPath("/tmp2/output.gv");

        try {
            DOTExporter.builder().build().exportToFile(machine, outputPath);
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
            DOTExporter.builder().build().exportToFile(machine, outputPath);
            fail("Error testing export failure");
        } catch (Exception e) {
            assertEquals(e.getClass(), FileExportException.class, "Exception mismatch");
        }
    }

    @Test
    public void testStateMachineWithWhiteSpaces() {
        String expected = "digraph G {" + NL +
                TB + "rankdir=\"LR\";" + NL +
                TB + "\"State A\" -> \"State B\" [label=\"Message 1\"];" + NL +
                "}";

        StateMachine machine = StateMachine.newBuilder()
                .from(State.from("State A").build()).to("State B").on("Message 1").build();

        String exportedString = DOTExporter.builder().build().export(machine);
        assertThat("Error comparing exported output", expected, is (exportedString));

    }

}

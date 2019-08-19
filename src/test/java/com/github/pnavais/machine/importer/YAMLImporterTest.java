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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Contains the unit tests for the YAML Importer
 */
public class YAMLImporterTest extends AbstractStateMachineTest {

    @Test
    public void testSimpleStateMachineParsing() {
        String input = "states:" + NL +
                " - state:" + NL +
                "     name: \"A\"" + NL +
                " - state:" + NL +
                "     name: \"B\"" + NL +
                "     current: \"true\"" + NL +
                "     properties: " + NL +
                "       color: \"#1122DD\"" + NL +
                " - state:" + NL +
                "     name: \"C\"" + NL +
                "     final: \"true\"" + NL +
                "     properties:" + NL +
                "       style: \"filled\"" + NL +
                "       fillcolor: \"#C2B3FF\"" + NL +
                " - state:" + NL +
                "     name: \"D\"" + NL +
                "     final: \"true\"" + NL +
                "     properties:" + NL +
                "       style: \"filled\"" + NL +
                "       fillcolor: \"#C2B3FF\"" + NL +
                "transitions:" + NL +
                "  - transition:" + NL +
                "      source:  \"A\"" + NL +
                "      target:  \"B\"" + NL +
                "      message: \"1\"" + NL +
                "  - transition:" + NL +
                "      source:  \"B\"" + NL +
                "      target:  \"D\"" + NL +
                "      message: \"2\"" + NL +
                "  - transition:" + NL +
                "      source:  \"B\"" + NL +
                "      target:  \"C\"";

        System.out.println(input);

        StateMachine stateMachine = YAMLImporter.builder().build().parse(input);
        assertNotNull(stateMachine, "Error building state machine");
    }


}

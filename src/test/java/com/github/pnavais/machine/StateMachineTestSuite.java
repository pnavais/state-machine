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

package com.github.pnavais.machine;

import com.github.pnavais.machine.builder.StateMachineBuilderTest;
import com.github.pnavais.machine.core.StateMachineComponentsTest;
import com.github.pnavais.machine.core.StateMachineCoreTest;
import com.github.pnavais.machine.core.StateMachineTraversalTest;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@IncludeEngines("junit-jupiter")
@SuiteDisplayName("State Machine tests")
@SelectClasses({StateMachineComponentsTest.class,
        StateMachineCoreTest.class,
        StateMachineTraversalTest. class,
        StateMachineBuilderTest.class})
public class StateMachineTestSuite {
}

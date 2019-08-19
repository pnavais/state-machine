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

import com.github.pnavais.machine.AbstractStateMachineTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Default base class for all exporter unit test
 * implementations.
 */
@Log
public abstract class AbstractExporterTest extends AbstractStateMachineTest {


    /** The tabulator character */
    protected static final char TB = '\t';

    /** The test file system */
    protected static FileSystem testFS = Jimfs.newFileSystem(Configuration.unix());

    @BeforeEach
    public void setup() {
        createDirectory(getOutputDirectory());
    }

    @AfterEach
    public void tearDown() {
        removeDirectory(getOutputDirectory());
    }

    /**
     * Retrieves the temporary output directory to
     * store in-memory output files.
     *
     * @return the outputdirectory
     */
    protected abstract String getOutputDirectory();

    /**
     * Creates a directory in the test file system
     * @param dir the directory to create
     */
    protected static void createDirectory(String dir) {
        Path outputDirPath = testFS.getPath(dir);
        try {
            Files.createDirectory(outputDirPath);
        } catch (IOException e) {
            fail("Error creating directory");
        }
    }

    /**
     * Removes the given directory and all its contained
     * files.
     * @param dir the directory to remove
     */
    protected static void removeDirectory(String dir) {
        Path testDir = testFS.getPath(dir);
        try {
            Files.walkFileTree(testDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.throwing(DOTExporterTest.class.getSimpleName(), "tearDown", e);
            fail("Error removing directory");
        }
    }
}

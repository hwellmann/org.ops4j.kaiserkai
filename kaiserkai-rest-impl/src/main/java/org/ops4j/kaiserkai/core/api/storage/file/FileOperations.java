/*
 * Copyright 2017 OPS4J Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.kaiserkai.core.api.storage.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 *
 */
public class FileOperations {

    private static Logger log = LoggerFactory.getLogger(FileOperations.class);

    public static void deleteTree(File dir) {
        try {
            Files.walk(dir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(java.nio.file.Path::toFile)
                .forEach(File::delete);
        } catch (IOException exc) {
            log.error("Error deleting directory {}", dir, exc);
        }
    }

    public static void createLinkFile(File linkDir, String digest) throws IOException {
        linkDir.mkdirs();
        File linkFile = new File(linkDir, "link");

        try (FileOutputStream os = new FileOutputStream(linkFile)) {
            os.write(digest.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static Optional<String> readLinkFile(File linkDir) {
        File link = new File(linkDir, "link");
        if (link.exists()) {
            try {
                return Optional.of(Files.readAllLines(link.toPath()).get(0));
            } catch (IOException exc) {
                log.debug("Error reading timestamp file {}", link, exc);
            }
        }
        return Optional.empty();
    }

    public static void createTimestampFile(File timestampDir) throws IOException {
        timestampDir.mkdirs();
        File timestampFile = new File(timestampDir, "startedat");
        String instant = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        try (FileOutputStream os = new FileOutputStream(timestampFile)) {
            os.write(instant.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void createDataFile(File uploadDir) throws IOException {
        uploadDir.mkdirs();
        File dataFile = new File(uploadDir, "data");
        dataFile.createNewFile();
    }

    public static Optional<Instant> readTimestampFile(File timestampDir) {
        File timestampFile = new File(timestampDir, "startedat");
        if (timestampFile.exists()) {
            try {
                String timestamp = Files.readAllLines(timestampFile.toPath()).get(0);
                Instant started = Instant.parse(timestamp);
                return Optional.of(started);
            } catch (IOException exc) {
                log.debug("Error reading timestamp file {}", timestampFile, exc);
            } catch (DateTimeParseException exc) {
                log.debug("Invalid timestamp file in {}", timestampDir, exc);
            }
        }
        return Optional.empty();

    }

    public static Stream<File> toSubDirs(File dir) {
        File[] subdirs = dir.listFiles();
        if (subdirs == null) {
            return Stream.empty();
        }
        return Stream.of(subdirs);
    }

    public static boolean isEmpty(File dir) {
        return dir.exists() && (dir.list().length == 0);
    }

    public static boolean isEmptyOrMissing(File dir) {
        return !dir.exists() || (dir.list().length == 0);
    }
}

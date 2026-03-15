package org.dynamisengine.session.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FundamentalTypeContractTest {
    private static final Pattern VECTRIX_DECLARATION = Pattern.compile(
            "(?m)^\\s*package\\s+.*\\.vectrix\\s*;|^\\s*(public\\s+)?(class|record|interface)\\s+(Vector2|Vector3|Quaternion|Matrix)\\w*\\b");

    @Test
    void repositoryMustNotDefineEntityIdTypeLocally() throws IOException {
        Path root = repositoryRoot();
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> matches = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("EntityId.java"))
                    .toList();
            assertTrue(matches.isEmpty(), "EntityId must come from DynamisCore; found: " + matches);
        }
    }

    @Test
    void repositoryMustNotDefineVectrixTypesLocally() throws IOException {
        Path root = repositoryRoot();
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> matches = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsVectrixTypeSignature)
                    .toList();
            assertTrue(matches.isEmpty(), "Vectrix types must not be defined in this repository; found: " + matches);
        }
    }

    private boolean containsVectrixTypeSignature(Path file) {
        try {
            String content = Files.readString(file);
            return VECTRIX_DECLARATION.matcher(content).find();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        Path cursor = current;
        while (cursor != null && !Files.exists(cursor.resolve(".git"))) {
            cursor = cursor.getParent();
        }
        return cursor == null ? current : cursor;
    }
}

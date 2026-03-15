package org.dynamisengine.session.core;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.session.api.model.ComponentEntry;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.EntityRecord;
import org.dynamisengine.session.api.model.SaveGame;
import org.dynamisengine.session.api.model.SaveMetadata;
import org.dynamisengine.session.core.io.SaveGameReader;
import org.dynamisengine.session.core.io.SaveGameWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaveGameIoRoundtripTest {

    @Test
    void writeThenReadShouldRoundtrip() {
        SaveGame original = sampleSaveGame();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new SaveGameWriter().write(out, original);

        SaveGame parsed = new SaveGameReader().read(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(original.metadata(), parsed.metadata());
        assertEquals(original.snapshot().entities().size(), parsed.snapshot().entities().size());
        for (int i = 0; i < original.snapshot().entities().size(); i++) {
            EntityRecord expectedEntity = original.snapshot().entities().get(i);
            EntityRecord actualEntity = parsed.snapshot().entities().get(i);
            assertEquals(expectedEntity.entityId(), actualEntity.entityId());
            assertEquals(expectedEntity.components().size(), actualEntity.components().size());
            for (int c = 0; c < expectedEntity.components().size(); c++) {
                ComponentEntry expectedComponent = expectedEntity.components().get(c);
                ComponentEntry actualComponent = actualEntity.components().get(c);
                assertEquals(expectedComponent.componentKeyId(), actualComponent.componentKeyId());
                assertArrayEquals(expectedComponent.payload(), actualComponent.payload());
            }
        }
    }

    @Test
    void readShouldFailOnVersionMismatch() {
        SaveGame original = sampleSaveGame();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new SaveGameWriter().write(out, original);
        byte[] bytes = out.toByteArray();

        // Overwrite formatVersion int right after 4-byte magic.
        ByteBuffer.wrap(bytes, 4, 4).putInt(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new SaveGameReader().read(new ByteArrayInputStream(bytes)));
        assertEquals("Unsupported save formatVersion: 2", ex.getMessage());
    }

    private SaveGame sampleSaveGame() {
        SaveMetadata metadata = new SaveMetadata(1, "1.0.0-SNAPSHOT", 1700000000000L, 42L, "slot-alpha");

        List<EntityRecord> entities = List.of(
                new EntityRecord(EntityId.of(1L), List.of(
                        new ComponentEntry("demo.name", "hero".getBytes(StandardCharsets.UTF_8)),
                        new ComponentEntry("demo.hp", ByteBuffer.allocate(4).putInt(100).array()))),
                new EntityRecord(EntityId.of(2L), List.of(
                        new ComponentEntry("demo.name", "mage".getBytes(StandardCharsets.UTF_8)),
                        new ComponentEntry("demo.hp", ByteBuffer.allocate(4).putInt(75).array())))
        );

        return new SaveGame(metadata, new EcsSnapshot(entities));
    }
}

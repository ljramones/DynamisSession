package org.dynamissession.core;

import org.dynamis.core.entity.EntityId;
import org.dynamisecs.core.DefaultWorld;
import org.dynamissession.api.model.ComponentEntry;
import org.dynamissession.api.model.EcsSnapshot;
import org.dynamissession.api.model.EntityRecord;
import org.dynamissession.core.codec.DefaultCodecRegistry;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultWorldSnapshotterImportTest {

    @Test
    void importShouldFailFastWhenCodecMissing() {
        DefaultWorld world = new DefaultWorld();
        EcsSnapshot snapshot = new EcsSnapshot(List.of(
                new EntityRecord(EntityId.of(10L), List.of(
                        new ComponentEntry("demo.hp", ByteBuffer.allocate(4).putInt(100).array())))));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new DefaultWorldSnapshotter().importSnapshot(world, snapshot, new DefaultCodecRegistry()));

        assertEquals("No codec registered for keyId: demo.hp", ex.getMessage());
    }
}

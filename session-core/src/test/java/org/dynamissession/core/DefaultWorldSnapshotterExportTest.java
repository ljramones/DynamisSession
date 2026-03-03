package org.dynamissession.core;

import org.dynamis.core.entity.EntityId;
import org.dynamisecs.api.component.ComponentKey;
import org.dynamisecs.core.DefaultWorld;
import org.dynamissession.api.codec.ComponentCodec;
import org.dynamissession.api.model.ComponentEntry;
import org.dynamissession.api.model.EcsSnapshot;
import org.dynamissession.api.model.EntityRecord;
import org.dynamissession.core.codec.DefaultCodecRegistry;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultWorldSnapshotterExportTest {

    private static final ComponentKey<String> NAME = ComponentKey.of("demo.name", String.class);
    private static final ComponentKey<Integer> HP = ComponentKey.of("demo.hp", Integer.class);

    @Test
    void exportShouldIncludeRegisteredComponents() {
        DefaultWorld world = new DefaultWorld();
        EntityId e1 = world.createEntity();
        EntityId e2 = world.createEntity();

        world.add(e1, NAME, "hero");
        world.add(e1, HP, 100);
        world.add(e2, NAME, "mage");
        world.add(e2, HP, 75);

        DefaultCodecRegistry registry = new DefaultCodecRegistry();
        registry.register(new StringCodec());
        registry.register(new IntCodec());

        EcsSnapshot snapshot = new DefaultWorldSnapshotter().exportSnapshot(world, registry);

        assertEquals(2, snapshot.entities().size());

        Map<EntityId, Map<String, byte[]>> byEntity = snapshot.entities().stream()
                .collect(Collectors.toMap(EntityRecord::entityId,
                        record -> record.components().stream().collect(Collectors.toMap(ComponentEntry::componentKeyId,
                                ComponentEntry::payload))));

        assertEquals("hero", new String(byEntity.get(e1).get("demo.name"), StandardCharsets.UTF_8));
        assertEquals(100, ByteBuffer.wrap(byEntity.get(e1).get("demo.hp")).getInt());

        assertEquals("mage", new String(byEntity.get(e2).get("demo.name"), StandardCharsets.UTF_8));
        assertEquals(75, ByteBuffer.wrap(byEntity.get(e2).get("demo.hp")).getInt());
    }

    private static final class StringCodec implements ComponentCodec<String> {

        @Override
        public String keyId() {
            return "demo.name";
        }

        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public byte[] encode(String value) {
            return value.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String decode(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static final class IntCodec implements ComponentCodec<Integer> {

        @Override
        public String keyId() {
            return "demo.hp";
        }

        @Override
        public Class<Integer> type() {
            return Integer.class;
        }

        @Override
        public byte[] encode(Integer value) {
            return ByteBuffer.allocate(4).putInt(value).array();
        }

        @Override
        public Integer decode(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getInt();
        }
    }
}

package org.dynamisengine.session.runtime;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ecs.api.component.ComponentKey;
import org.dynamisengine.ecs.api.world.World;
import org.dynamisengine.ecs.core.DefaultWorld;
import org.dynamisengine.session.api.codec.ComponentCodec;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.SaveGame;
import org.dynamisengine.session.api.model.SaveMetadata;
import org.dynamisengine.session.core.codec.DefaultCodecRegistry;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionManagerRoundtripTest {

    private static final ComponentKey<String> NAME = ComponentKey.of("demo.name", String.class);
    private static final ComponentKey<Integer> HP = ComponentKey.of("demo.hp", Integer.class);

    @Test
    void saveAndLoadShouldPreserveEntityIdsAndComponents() throws Exception {
        DefaultSessionManager manager = new DefaultSessionManager();
        DefaultCodecRegistry registry = fullRegistry();

        DefaultWorld world = (DefaultWorld) manager.newGame();
        EntityId e1 = world.createEntity();
        EntityId removed = world.createEntity();
        EntityId e3 = world.createEntity();
        world.destroyEntity(removed);

        world.add(e1, NAME, "hero");
        world.add(e1, HP, 100);
        world.add(e3, NAME, "mage");
        world.add(e3, HP, 75);

        SaveGame save = new SaveGame(
                new SaveMetadata(1, "1.0.0-SNAPSHOT", 1700000001000L, 12L, "slot-a"),
                new EcsSnapshot(List.of()));

        Path slot = Files.createTempFile("dynamis-session-", ".dses");
        manager.save(slot, world, save, registry);

        World loadedWorld = manager.load(slot, registry);

        assertTrue(loadedWorld.exists(e1));
        assertTrue(loadedWorld.exists(e3));
        assertFalse(loadedWorld.exists(removed));
        assertEquals(2, loadedWorld.entities().size());

        assertEquals("hero", loadedWorld.get(e1, NAME).orElseThrow());
        assertEquals(100, loadedWorld.get(e1, HP).orElseThrow());
        assertEquals("mage", loadedWorld.get(e3, NAME).orElseThrow());
        assertEquals(75, loadedWorld.get(e3, HP).orElseThrow());
    }

    @Test
    void loadShouldFailWhenCodecMissing() throws Exception {
        DefaultSessionManager manager = new DefaultSessionManager();

        DefaultCodecRegistry saveRegistry = fullRegistry();
        DefaultWorld world = (DefaultWorld) manager.newGame();
        EntityId e1 = world.createEntity();
        world.add(e1, NAME, "hero");
        world.add(e1, HP, 100);

        SaveGame save = new SaveGame(
                new SaveMetadata(1, "1.0.0-SNAPSHOT", 1700000001000L, 12L, "slot-b"),
                new EcsSnapshot(List.of()));

        Path slot = Files.createTempFile("dynamis-session-", ".dses");
        manager.save(slot, world, save, saveRegistry);

        DefaultCodecRegistry loadRegistry = new DefaultCodecRegistry();
        loadRegistry.register(new StringCodec());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> manager.load(slot, loadRegistry));
        assertEquals("No codec registered for keyId: demo.hp", ex.getMessage());
    }

    @Test
    void loadShouldFailOnVersionMismatch() throws Exception {
        DefaultSessionManager manager = new DefaultSessionManager();
        DefaultCodecRegistry registry = fullRegistry();

        DefaultWorld world = (DefaultWorld) manager.newGame();
        EntityId e1 = world.createEntity();
        world.add(e1, NAME, "hero");
        world.add(e1, HP, 100);

        SaveGame save = new SaveGame(
                new SaveMetadata(1, "1.0.0-SNAPSHOT", 1700000001000L, 12L, "slot-c"),
                new EcsSnapshot(List.of()));

        Path slot = Files.createTempFile("dynamis-session-", ".dses");
        manager.save(slot, world, save, registry);

        byte[] bytes = Files.readAllBytes(slot);
        ByteBuffer.wrap(bytes, 4, 4).putInt(2);
        Files.write(slot, bytes);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> manager.load(slot, registry));
        assertEquals("Unsupported save formatVersion: 2", ex.getMessage());
    }

    private DefaultCodecRegistry fullRegistry() {
        DefaultCodecRegistry registry = new DefaultCodecRegistry();
        registry.register(new StringCodec());
        registry.register(new IntCodec());
        return registry;
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

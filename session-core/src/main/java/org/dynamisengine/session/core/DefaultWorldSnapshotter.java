package org.dynamisengine.session.core;

import org.dynamis.core.entity.EntityId;
import org.dynamisecs.api.component.ComponentKey;
import org.dynamisecs.api.world.World;
import org.dynamisengine.session.api.WorldSnapshotter;
import org.dynamisengine.session.api.codec.CodecRegistry;
import org.dynamisengine.session.api.codec.ComponentCodec;
import org.dynamisengine.session.api.model.ComponentEntry;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.EntityRecord;
import org.dynamisengine.session.core.codec.DefaultCodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultWorldSnapshotter implements WorldSnapshotter {

    @Override
    public EcsSnapshot exportSnapshot(World world, CodecRegistry registry) {
        Objects.requireNonNull(world, "world");
        DefaultCodecRegistry codecRegistry = requireDefaultRegistry(registry);

        List<EntityRecord> entities = new ArrayList<>();
        for (EntityId entityId : world.entities()) {
            List<ComponentEntry> components = new ArrayList<>();
            for (ComponentCodec<?> codec : codecRegistry.codecs()) {
                ComponentEntry entry = exportComponent(world, entityId, codec);
                if (entry != null) {
                    components.add(entry);
                }
            }
            entities.add(new EntityRecord(entityId, components));
        }
        return new EcsSnapshot(entities);
    }

    @Override
    public void importSnapshot(World world, EcsSnapshot snapshot, CodecRegistry registry) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(registry, "registry");

        for (EntityRecord record : snapshot.entities()) {
            EntityId importedId = world.createEntity();
            for (ComponentEntry entry : record.components()) {
                ComponentCodec<?> codec = registry.find(entry.componentKeyId())
                        .orElseThrow(() -> new IllegalStateException("No codec registered for keyId: " + entry.componentKeyId()));
                addDecodedComponent(world, importedId, codec, entry.payload());
            }
        }
    }

    private DefaultCodecRegistry requireDefaultRegistry(CodecRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        if (registry instanceof DefaultCodecRegistry defaultRegistry) {
            return defaultRegistry;
        }
        throw new IllegalArgumentException("DefaultWorldSnapshotter requires DefaultCodecRegistry for export enumeration");
    }

    private ComponentEntry exportComponent(World world, EntityId entityId, ComponentCodec<?> codec) {
        ComponentKey<Object> key = ComponentKey.of(codec.keyId(), cast(codec.type()));
        return world.get(entityId, key)
                .map(value -> new ComponentEntry(codec.keyId(), encode(codec, value)))
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <T> byte[] encode(ComponentCodec<?> codec, Object value) {
        return ((ComponentCodec<T>) codec).encode((T) value);
    }

    @SuppressWarnings("unchecked")
    private <T> void addDecodedComponent(World world, EntityId entityId, ComponentCodec<?> codec, byte[] payload) {
        ComponentCodec<T> typedCodec = (ComponentCodec<T>) codec;
        ComponentKey<T> key = ComponentKey.of(codec.keyId(), typedCodec.type());
        T decoded = typedCodec.decode(payload);
        world.add(entityId, key, decoded);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(Class<?> rawType) {
        return (Class<T>) rawType;
    }
}

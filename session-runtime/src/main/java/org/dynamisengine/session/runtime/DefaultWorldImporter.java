package org.dynamisengine.session.runtime;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ecs.api.component.ComponentKey;
import org.dynamisengine.ecs.core.DefaultWorld;
import org.dynamisengine.session.api.codec.CodecRegistry;
import org.dynamisengine.session.api.codec.ComponentCodec;
import org.dynamisengine.session.api.model.ComponentEntry;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.EntityRecord;
import org.dynamisengine.session.core.DefaultWorldSnapshotter;

import java.util.Objects;

public final class DefaultWorldImporter {

    public void importInto(DefaultWorld world,
                           EcsSnapshot snapshot,
                           CodecRegistry registry,
                           DefaultWorldSnapshotter snapshotter) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(snapshotter, "snapshotter");

        for (EntityRecord record : snapshot.entities()) {
            EntityId targetId = record.entityId();
            EntityId matched = createMatchingEntity(world, targetId);

            for (ComponentEntry entry : record.components()) {
                ComponentCodec<?> codec = registry.find(entry.componentKeyId())
                        .orElseThrow(() -> new IllegalStateException("No codec registered for keyId: " + entry.componentKeyId()));
                addDecodedComponent(world, matched, codec, entry.payload());
            }
        }
    }

    private EntityId createMatchingEntity(DefaultWorld world, EntityId targetId) {
        while (true) {
            EntityId created = world.createEntity();
            if (created.equals(targetId)) {
                return created;
            }
            if (created.id() > targetId.id()) {
                throw new IllegalStateException("Cannot create entity with preserved id " + targetId.id()
                        + "; reached " + created.id());
            }
            world.destroyEntity(created);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addDecodedComponent(DefaultWorld world, EntityId entityId, ComponentCodec<?> codec, byte[] payload) {
        ComponentCodec<T> typedCodec = (ComponentCodec<T>) codec;
        ComponentKey<T> key = ComponentKey.of(codec.keyId(), typedCodec.type());
        T decoded = typedCodec.decode(payload);
        world.add(entityId, key, decoded);
    }
}

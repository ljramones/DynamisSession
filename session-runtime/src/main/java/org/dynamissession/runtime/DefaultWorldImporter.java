package org.dynamissession.runtime;

import org.dynamis.core.entity.EntityId;
import org.dynamisecs.api.component.ComponentKey;
import org.dynamisecs.core.DefaultWorld;
import org.dynamissession.api.codec.CodecRegistry;
import org.dynamissession.api.codec.ComponentCodec;
import org.dynamissession.api.model.ComponentEntry;
import org.dynamissession.api.model.EcsSnapshot;
import org.dynamissession.api.model.EntityRecord;
import org.dynamissession.core.DefaultWorldSnapshotter;

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

package org.dynamisengine.session.api.model;

import org.dynamisengine.core.entity.EntityId;

import java.util.List;
import java.util.Objects;

public record EntityRecord(EntityId entityId, List<ComponentEntry> components) {
    public EntityRecord {
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(components, "components");
    }
}

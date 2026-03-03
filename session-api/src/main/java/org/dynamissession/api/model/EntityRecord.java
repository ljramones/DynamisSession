package org.dynamissession.api.model;

import org.dynamis.core.entity.EntityId;

import java.util.List;
import java.util.Objects;

public record EntityRecord(EntityId entityId, List<ComponentEntry> components) {
    public EntityRecord {
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(components, "components");
    }
}

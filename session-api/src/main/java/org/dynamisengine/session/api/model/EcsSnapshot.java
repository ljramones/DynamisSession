package org.dynamisengine.session.api.model;

import java.util.List;
import java.util.Objects;

public record EcsSnapshot(List<EntityRecord> entities) {
    public EcsSnapshot {
        Objects.requireNonNull(entities, "entities");
    }
}

package org.dynamisengine.session.api.model;

import java.util.Objects;

public record ComponentEntry(String componentKeyId, byte[] payload) {
    public ComponentEntry {
        Objects.requireNonNull(componentKeyId, "componentKeyId");
        if (componentKeyId.isBlank()) {
            throw new IllegalArgumentException("componentKeyId must not be blank");
        }
        Objects.requireNonNull(payload, "payload");
    }
}

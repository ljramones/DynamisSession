package org.dynamissession.api.model;

import java.util.Objects;

public record SaveGame(SaveMetadata metadata, EcsSnapshot snapshot) {
    public SaveGame {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(snapshot, "snapshot");
    }
}

package org.dynamissession.api.model;

import org.dynamis.core.exception.DynamisException;

import java.util.Objects;

public record SaveMetadata(
        int formatVersion,
        String buildVersion,
        long createdEpochMillis,
        long worldTick,
        String slotName
) {
    public SaveMetadata {
        if (formatVersion <= 0) {
            throw new DynamisException("formatVersion must be > 0");
        }
        if (createdEpochMillis <= 0) {
            throw new DynamisException("createdEpochMillis must be > 0");
        }
        Objects.requireNonNull(buildVersion, "buildVersion");
        if (buildVersion.isBlank()) {
            throw new DynamisException("buildVersion must not be blank");
        }
        Objects.requireNonNull(slotName, "slotName");
        if (slotName.isBlank()) {
            throw new DynamisException("slotName must not be blank");
        }
    }
}

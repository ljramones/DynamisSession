package org.dynamissession.api.model;

import org.dynamis.core.exception.DynamisException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SaveMetadataTest {

    @Test
    void formatVersionMustBePositive() {
        assertThrows(DynamisException.class,
                () -> new SaveMetadata(0, "1.0.0", 1L, 0L, "slot-1"));
    }

    @Test
    void buildVersionMustNotBeBlank() {
        assertThrows(DynamisException.class,
                () -> new SaveMetadata(1, " ", 1L, 0L, "slot-1"));
    }

    @Test
    void slotNameMustNotBeBlank() {
        assertThrows(DynamisException.class,
                () -> new SaveMetadata(1, "1.0.0", 1L, 0L, " "));
    }
}

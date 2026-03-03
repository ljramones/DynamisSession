package org.dynamissession.core;

import org.dynamissession.api.codec.ComponentCodec;
import org.dynamissession.core.codec.DefaultCodecRegistry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCodecRegistryTest {

    @Test
    void registerAndFindShouldWork() {
        DefaultCodecRegistry registry = new DefaultCodecRegistry();
        ComponentCodec<String> codec = new StringCodec("demo.name");

        registry.register(codec);

        assertTrue(registry.find("demo.name").isPresent());
        assertSame(codec, registry.find("demo.name").orElseThrow());
    }

    @Test
    void duplicateRegistrationShouldFail() {
        DefaultCodecRegistry registry = new DefaultCodecRegistry();
        registry.register(new StringCodec("demo.name"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.register(new StringCodec("demo.name")));
        assertEquals("Duplicate codec registration for keyId: demo.name", ex.getMessage());
    }

    private record StringCodec(String keyId) implements ComponentCodec<String> {

        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public byte[] encode(String value) {
            return value.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String decode(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}

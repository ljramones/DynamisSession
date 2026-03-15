package org.dynamisengine.session.api;

import org.dynamisengine.session.api.codec.CodecRegistry;
import org.dynamisengine.session.api.codec.ComponentCodec;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodecRegistryContractTest {

    @Test
    void registerAndFindShouldWork() {
        CodecRegistry registry = new InMemoryRegistry();
        ComponentCodec<String> codec = new StringCodec("test.component");

        registry.register(codec);

        Optional<ComponentCodec<?>> found = registry.find("test.component");
        assertTrue(found.isPresent());
        assertSame(codec, found.orElseThrow());
    }

    private static final class InMemoryRegistry implements CodecRegistry {
        private final Map<String, ComponentCodec<?>> codecs = new HashMap<>();

        @Override
        public void register(ComponentCodec<?> codec) {
            codecs.put(codec.keyId(), codec);
        }

        @Override
        public Optional<ComponentCodec<?>> find(String keyId) {
            return Optional.ofNullable(codecs.get(keyId));
        }
    }

    private record StringCodec(String keyId) implements ComponentCodec<String> {

        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public byte[] encode(String value) {
            return value.getBytes();
        }

        @Override
        public String decode(byte[] bytes) {
            return new String(bytes);
        }
    }
}

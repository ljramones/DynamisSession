package org.dynamissession.core.codec;

import org.dynamissession.api.codec.CodecRegistry;
import org.dynamissession.api.codec.ComponentCodec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultCodecRegistry implements CodecRegistry {

    private final Map<String, ComponentCodec<?>> codecsByKey = new LinkedHashMap<>();

    @Override
    public void register(ComponentCodec<?> codec) {
        Objects.requireNonNull(codec, "codec");
        String keyId = codec.keyId();
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("codec.keyId() must not be null/blank");
        }
        if (codecsByKey.containsKey(keyId)) {
            throw new IllegalArgumentException("Duplicate codec registration for keyId: " + keyId);
        }
        codecsByKey.put(keyId, codec);
    }

    @Override
    public Optional<ComponentCodec<?>> find(String keyId) {
        Objects.requireNonNull(keyId, "keyId");
        return Optional.ofNullable(codecsByKey.get(keyId));
    }

    public Collection<ComponentCodec<?>> codecs() {
        return codecsByKey.values();
    }
}

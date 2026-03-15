package org.dynamisengine.session.api.codec;

import java.util.Optional;

public interface CodecRegistry {

    void register(ComponentCodec<?> codec);

    Optional<ComponentCodec<?>> find(String keyId);
}

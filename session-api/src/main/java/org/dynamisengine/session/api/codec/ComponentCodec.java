package org.dynamisengine.session.api.codec;

public interface ComponentCodec<T> {

    /** Stable component id used on disk. Must match ComponentKey.id(). */
    String keyId();

    Class<T> type();

    byte[] encode(T value);

    T decode(byte[] bytes);
}

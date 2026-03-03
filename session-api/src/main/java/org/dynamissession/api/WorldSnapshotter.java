package org.dynamissession.api;

import org.dynamisecs.api.world.World;
import org.dynamissession.api.codec.CodecRegistry;
import org.dynamissession.api.model.EcsSnapshot;

public interface WorldSnapshotter {

    EcsSnapshot exportSnapshot(World world, CodecRegistry registry);

    void importSnapshot(World world, EcsSnapshot snapshot, CodecRegistry registry);
}

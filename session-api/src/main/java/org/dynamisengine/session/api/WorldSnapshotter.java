package org.dynamisengine.session.api;

import org.dynamisecs.api.world.World;
import org.dynamisengine.session.api.codec.CodecRegistry;
import org.dynamisengine.session.api.model.EcsSnapshot;

public interface WorldSnapshotter {

    EcsSnapshot exportSnapshot(World world, CodecRegistry registry);

    void importSnapshot(World world, EcsSnapshot snapshot, CodecRegistry registry);
}

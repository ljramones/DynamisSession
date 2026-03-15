package org.dynamisengine.session.api;

import org.dynamisengine.ecs.api.world.World;
import org.dynamisengine.session.api.codec.CodecRegistry;
import org.dynamisengine.session.api.model.SaveGame;

import java.nio.file.Path;

public interface SessionManager {

    World newGame();

    void save(Path slotFile, World world, SaveGame save, CodecRegistry registry);

    World load(Path slotFile, CodecRegistry registry);
}

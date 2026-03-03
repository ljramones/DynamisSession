package org.dynamissession.api;

import org.dynamisecs.api.world.World;
import org.dynamissession.api.codec.CodecRegistry;
import org.dynamissession.api.model.SaveGame;

import java.nio.file.Path;

public interface SessionManager {

    World newGame();

    void save(Path slotFile, World world, SaveGame save, CodecRegistry registry);

    World load(Path slotFile, CodecRegistry registry);
}

package org.dynamissession.runtime;

import org.dynamisecs.api.world.World;
import org.dynamisecs.core.DefaultWorld;
import org.dynamissession.api.SessionManager;
import org.dynamissession.api.codec.CodecRegistry;
import org.dynamissession.api.model.SaveGame;
import org.dynamissession.core.DefaultWorldSnapshotter;
import org.dynamissession.core.io.SaveGameReader;
import org.dynamissession.core.io.SaveGameWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class DefaultSessionManager implements SessionManager {

    private final DefaultWorldSnapshotter snapshotter;
    private final SaveGameWriter writer;
    private final SaveGameReader reader;
    private final DefaultWorldImporter importer;

    public DefaultSessionManager() {
        this(new DefaultWorldSnapshotter(), new SaveGameWriter(), new SaveGameReader(), new DefaultWorldImporter());
    }

    DefaultSessionManager(DefaultWorldSnapshotter snapshotter,
                          SaveGameWriter writer,
                          SaveGameReader reader,
                          DefaultWorldImporter importer) {
        this.snapshotter = Objects.requireNonNull(snapshotter, "snapshotter");
        this.writer = Objects.requireNonNull(writer, "writer");
        this.reader = Objects.requireNonNull(reader, "reader");
        this.importer = Objects.requireNonNull(importer, "importer");
    }

    @Override
    public World newGame() {
        return new DefaultWorld();
    }

    @Override
    public void save(Path slotFile, World world, SaveGame save, CodecRegistry registry) {
        Objects.requireNonNull(slotFile, "slotFile");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(save, "save");
        Objects.requireNonNull(registry, "registry");

        SaveGame toWrite = new SaveGame(save.metadata(), snapshotter.exportSnapshot(world, registry));

        try {
            Path parent = slotFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(slotFile)) {
                writer.write(out, toWrite);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save slot file: " + slotFile, e);
        }
    }

    @Override
    public World load(Path slotFile, CodecRegistry registry) {
        Objects.requireNonNull(slotFile, "slotFile");
        Objects.requireNonNull(registry, "registry");

        SaveGame save;
        try (InputStream in = Files.newInputStream(slotFile)) {
            save = reader.read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load slot file: " + slotFile, e);
        }

        DefaultWorld world = new DefaultWorld();
        importer.importInto(world, save.snapshot(), registry, snapshotter);
        return world;
    }
}

package org.dynamisengine.session.core.io;

import org.dynamis.core.entity.EntityId;
import org.dynamisengine.session.api.model.ComponentEntry;
import org.dynamisengine.session.api.model.EcsSnapshot;
import org.dynamisengine.session.api.model.EntityRecord;
import org.dynamisengine.session.api.model.SaveGame;
import org.dynamisengine.session.api.model.SaveMetadata;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SaveGameReader {

    private static final byte[] MAGIC = new byte[]{'D', 'S', 'E', 'S'};

    public SaveGame read(InputStream in) {
        Objects.requireNonNull(in, "in");

        try {
            DataInputStream data = new DataInputStream(in);
            validateMagic(data);

            int formatVersion = data.readInt();
            if (formatVersion != 1) {
                throw new IllegalArgumentException("Unsupported save formatVersion: " + formatVersion);
            }

            SaveMetadata metadata = new SaveMetadata(
                    formatVersion,
                    data.readUTF(),
                    data.readLong(),
                    data.readLong(),
                    data.readUTF());

            int entityCount = data.readInt();
            List<EntityRecord> entities = new ArrayList<>(entityCount);
            for (int i = 0; i < entityCount; i++) {
                EntityId entityId = EntityId.of(data.readLong());
                int componentCount = data.readInt();
                List<ComponentEntry> components = new ArrayList<>(componentCount);
                for (int c = 0; c < componentCount; c++) {
                    String keyId = data.readUTF();
                    int payloadLen = data.readInt();
                    if (payloadLen < 0) {
                        throw new IllegalArgumentException("Invalid payload length: " + payloadLen);
                    }
                    byte[] payload = data.readNBytes(payloadLen);
                    if (payload.length != payloadLen) {
                        throw new IllegalArgumentException("Unexpected end of stream while reading payload for keyId: " + keyId);
                    }
                    components.add(new ComponentEntry(keyId, payload));
                }
                entities.add(new EntityRecord(entityId, components));
            }

            return new SaveGame(metadata, new EcsSnapshot(entities));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read save game", e);
        }
    }

    private void validateMagic(DataInputStream data) throws IOException {
        byte[] actual = data.readNBytes(MAGIC.length);
        if (actual.length != MAGIC.length) {
            throw new IllegalArgumentException("Invalid save header: missing magic bytes");
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (actual[i] != MAGIC[i]) {
                throw new IllegalArgumentException("Invalid save header: bad magic bytes");
            }
        }
    }
}

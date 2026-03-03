package org.dynamissession.core.io;

import org.dynamissession.api.model.ComponentEntry;
import org.dynamissession.api.model.EntityRecord;
import org.dynamissession.api.model.SaveGame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public final class SaveGameWriter {

    private static final byte[] MAGIC = new byte[]{'D', 'S', 'E', 'S'};

    public void write(OutputStream out, SaveGame save) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(save, "save");

        try {
            DataOutputStream data = new DataOutputStream(out);
            data.write(MAGIC);
            data.writeInt(save.metadata().formatVersion());
            data.writeUTF(save.metadata().buildVersion());
            data.writeLong(save.metadata().createdEpochMillis());
            data.writeLong(save.metadata().worldTick());
            data.writeUTF(save.metadata().slotName());

            data.writeInt(save.snapshot().entities().size());
            for (EntityRecord entity : save.snapshot().entities()) {
                data.writeLong(entity.entityId().id());
                data.writeInt(entity.components().size());
                for (ComponentEntry component : entity.components()) {
                    data.writeUTF(component.componentKeyId());
                    data.writeInt(component.payload().length);
                    data.write(component.payload());
                }
            }
            data.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write save game", e);
        }
    }
}

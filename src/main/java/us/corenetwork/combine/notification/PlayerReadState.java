package us.corenetwork.combine.notification;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "readStates")
public class PlayerReadState {
    @DatabaseField
    private UUID player;
    @DatabaseField
    private long read;

    public PlayerReadState(UUID player, long read) {
        this.player = player;
        this.read = read;
    }

    public UUID getPlayer() {
        return player;
    }

    public long getRead() {
        return read;
    }
}

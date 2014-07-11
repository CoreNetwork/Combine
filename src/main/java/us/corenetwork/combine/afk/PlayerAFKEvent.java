package us.corenetwork.combine.afk;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerAFKEvent extends PlayerEvent {
    private HandlerList handlerList = new HandlerList();
    private boolean afk;

    public PlayerAFKEvent(Player who, boolean afk) {
        super(who);
        this.afk = afk;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public boolean isAfk() {
        return afk;
    }
}

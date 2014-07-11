package us.corenetwork.combine.afk;

import org.bukkit.entity.Player;

/**
 * API facade for player AFK detection
 */
public interface CombineAFK {
    public boolean isPlayerAFK(Player player);
}

package us.corenetwork.combine.afk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import us.corenetwork.combine.CombinePlugin;

public class SimpleAFKManager implements CombineAFK, Listener {
    private CombinePlugin plugin;
    private static final String LAST_MOVEMENT = "lastMovement";
    private static final String AFK = "afk";
    private long minAFKDetectionThreshold = 60000; //TODO config
    private long maxAFK = 60 * 60 * 1000; // 1 hour

    public SimpleAFKManager(CombinePlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasMetadata(LAST_MOVEMENT)) {
                        long last = player.getMetadata(LAST_MOVEMENT).get(0).asLong();
                        long diff = System.currentTimeMillis() - last;
                        if (diff >= minAFKDetectionThreshold && !isPlayerAFK(player)) {
                            Bukkit.getPluginManager().callEvent(new PlayerAFKEvent(player, true));
                            player.sendMessage(ChatColor.DARK_RED + "You are now AFK.");
                            player.setMetadata(AFK, new FixedMetadataValue(plugin, true));
                        }
                        if (diff >= maxAFK) {
                            player.kickPlayer("You were afk for too long");
                        }
                    }
                }
            }
        }, 20, 20);
    }

    @Override
    public boolean isPlayerAFK(Player player) {
        return player.hasMetadata(AFK) && player.getMetadata(AFK).get(0).asBoolean();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        boolean realMove = moveEvent.getFrom().getYaw() != moveEvent.getTo().getYaw() || moveEvent.getFrom().getPitch() != moveEvent.getTo().getPitch();
        if (realMove) {
            Player player = moveEvent.getPlayer();
            if (isPlayerAFK(player)) {
                Bukkit.getPluginManager().callEvent(new PlayerAFKEvent(player, false));
                player.sendMessage(ChatColor.GREEN + "You are no longer AFK.");
                player.setMetadata(AFK, new FixedMetadataValue(plugin, false));
            }
            player.setMetadata(LAST_MOVEMENT, new FixedMetadataValue(plugin, System.currentTimeMillis()));
        }
    }
}

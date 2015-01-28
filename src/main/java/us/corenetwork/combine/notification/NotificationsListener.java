package us.corenetwork.combine.notification;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NotificationsListener implements Listener {
    CombineNotifications notifications;

    public NotificationsListener(CombineNotifications notifications) {
        this.notifications = notifications;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        notifications.getSummaryManager().sendSummary(event.getPlayer());
    }
}

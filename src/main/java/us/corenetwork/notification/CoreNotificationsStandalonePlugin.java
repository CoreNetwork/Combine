package us.corenetwork.notification;

import org.bukkit.plugin.java.JavaPlugin;

public class CoreNotificationsStandalonePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        CoreNotifications.bootstrap(this);
        SimpleCoreNotifications.log.info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        CoreNotifications.shutdown();
        SimpleCoreNotifications.log.info("Plugin disabled");
    }
}

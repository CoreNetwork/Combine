package us.corenetwork.combine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import us.corenetwork.combine.afk.CombineAFK;
import us.corenetwork.combine.afk.SimpleAFKManager;
import us.corenetwork.combine.notification.CombineNotifications;

public class CombinePlugin extends JavaPlugin {
    public static CombinePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        SimpleAFKManager afk = new SimpleAFKManager(this);
        Bukkit.getServicesManager().register(CombineAFK.class, afk, this, ServicePriority.High);
        CombineNotifications.bootstrap(this);
    }

    @Override
    public void onDisable() {
        CombineNotifications.shutdown();
    }
}

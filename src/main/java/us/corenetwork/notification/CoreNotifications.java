package us.corenetwork.notification;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * API facade for the notifications API
 * <br/>
 * This is registered to the Bukkit Service Manager, so get the singleton instance by calling:
 * <pre>
 *     Bukkit.getServicesManager().getRegistration(CoreNotifications.class).getProvider();
 * </pre>
 */
public abstract class CoreNotifications {
    /**
     * Creates the CoreNotification singleton, if not exists, and sets up database connection
     * <br/>
     * Call this in the onEnable method of your plugin.
     * @param plugin the instance of the plugin which manages this instance.
     */
    public static void bootstrap(JavaPlugin plugin) {
        if (SimpleCoreNotifications.registered || Bukkit.getServicesManager().isProvidedFor(CoreNotifications.class)) {
            SimpleCoreNotifications.registered = true;
            return;
        }
        SimpleCoreNotifications instance = new SimpleCoreNotifications();

        Bukkit.getServicesManager().register(CoreNotifications.class, instance, plugin, ServicePriority.High);

        plugin.getCommand("n").setExecutor(new NotificationCommandExecutor(instance));

        SimpleCoreNotifications.registered = true;
    }

    /**
     * Safely disconnects from the database and unregisters the service.
     * <br/>
     * Call this in the onDisable method of your plugin.
     */
    public static void shutdown() {
        if (SimpleCoreNotifications.shutdown) {
            return;
        }

        SimpleCoreNotifications instance = (SimpleCoreNotifications) Bukkit.getServicesManager().getRegistration(CoreNotifications.class).getProvider();
        try {
            instance.connectionSource.close();
        } catch (SQLException e) {
            SimpleCoreNotifications.log.log(Level.SEVERE, "SQL Error while closing", e);
        }
        Bukkit.getServicesManager().unregister(instance);

        SimpleCoreNotifications.shutdown = true;
    }

    /**
     * @param player the UUID of the player
     * @return a list of all notifications for the given player, sorted by time of creation, descending
     */
    public abstract List<Notification> getNotificationsForPlayer(UUID player);

    /**
     * Stores a notification in the database.
     * When the player of the notification is online, the notification is just printed
     * @param notification the notification to store.
     */
    public abstract void createNotification(Notification notification);

    /**
     * @param player the name or UUID of the player.
     * @param template the template of the notifications.
     * @return a list of notifications that use the given template and haven't been read by the given player yet.
     */
    public abstract List<Notification> getNotificationsUnreadForPlayerWithTemplate(UUID player, Template template);

    /**
     * Registers or updates a template in the database.
     * @param plugin the plugin that uses this template.
     * @param code a code name to differ from other templates of your plugin.
     * @param template the actual template. Variables are written like this: '&lt;Variable&gt;'; you can have as many variables as you like.
     * @return an instance of the template to use with notifications later.
     */
    public abstract Template registerTemplate(Plugin plugin, String code, String template);

    /**
     * Saves any changes made to the given notification
     * @param n the notification to update in the database.
     */
    public abstract void saveNotification(Notification n);

    /**
     * The summary manager prints summaries to players and manages the SummaryGenerators from plugins.
     * @return the SummaryManager
     */
    public abstract SummaryManager getSummaryManager();
}

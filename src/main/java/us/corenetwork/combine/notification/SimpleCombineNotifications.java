package us.corenetwork.combine.notification;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.corenetwork.combine.afk.CombineAFK;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleCombineNotifications extends CombineNotifications {
    /**
     * Holds if there is already an instance registered in the Bukkit service manager
     */
    static boolean registered = false;
    /**
     * Holds if the system has been shut down, so it won't try to disconnect from the database twice
     */
    static boolean shutdown = false;

    /**
     * Logger instance
     */
    static Logger log = Logger.getLogger("CombineNotifications");
    ConnectionSource connectionSource;
    /**
     * Data directory for anything
     */
    private File baseDir;
    private Dao<Notification, Integer> notificationIntegerDao;
    private Dao<Template, Integer> templateIntegerDao;
    private SummaryManager summaryManager = new SummaryManager(this);
    private CombineAFK afk;


    /**
     * Constructor for the singleton notifications API facade. Do not use.
     */
    SimpleCombineNotifications() {
        baseDir = new File("./plugins/NotificationStore/");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Can't find SQLite JDBC class, notifications won't work.", e);
            return;
        }
        String dbUrl = "jdbc:sqlite:" + baseDir.getAbsolutePath() + File.separator +  "data.sqlite";
        afk = Bukkit.getServicesManager().getRegistration(CombineAFK.class).getProvider();
        try {
            connectionSource = new JdbcConnectionSource(dbUrl);
            notificationIntegerDao = DaoManager.createDao(connectionSource, Notification.class);
            templateIntegerDao = DaoManager.createDao(connectionSource, Template.class);
            TableUtils.createTableIfNotExists(connectionSource, Notification.class);
            TableUtils.createTableIfNotExists(connectionSource, Template.class);
        } catch (SQLException e) {
            logSQLError(e);
        }
    }

    /**
     * @param player the name or UUID of the player
     * @return a list of all notifications for the given player, sorted by time of creation, descending
     */
    @Override
    public List<Notification> getNotificationsForPlayer(UUID player) {
        try {
            return notificationIntegerDao.queryForEq("player", player); //TODO order by time desc
        } catch (SQLException e) {
            logSQLError(e);
            return Collections.emptyList();
        }
    }

    /**
     * Stores a notification in the database.
     * When the player of the notification is online, the notification is just printed
     * @param notification the notification to store.
     */
    @Override
    public void createNotification(Notification notification) {
        Player p = Bukkit.getPlayer(notification.getPlayer()); //TODO uuid
        if (p != null && p.isOnline()) {
            showNotification(notification, false);
        }
        try {
            notificationIntegerDao.createIfNotExists(notification);
        } catch (SQLException e) {
            logSQLError(e);
        }
    }

    /**
     * @param player the name or UUID of the player.
     * @param template the template of the notifications.
     * @return a list of notifications that use the given template and haven't been read by the given player yet.
     */
    @Override
    public List<Notification> getNotificationsUnreadForPlayerWithTemplate(UUID player, Template template) {
        try {
            QueryBuilder<Notification, Integer> queryBuilder = notificationIntegerDao.queryBuilder();  //TODO cache query
            queryBuilder.where().eq("player", player).isNull("read").eq("template_id", templateIntegerDao.extractId(template)).and(3);
            queryBuilder.orderBy("time", false);
            return notificationIntegerDao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            logSQLError(e);
            return Collections.emptyList();
        }
    }

    /**
     * Registers or updates a template in the database.
     * @param plugin the plugin that uses this template.
     * @param code a code name to differ from other templates of your plugin.
     * @param template the actual template. Variables are written like this: '&lt;Variable&gt;'; you can have as many variables as you like.
     * @return an instance of the template to use with notifications later.
     */
    @Override
    public Template registerTemplate(Plugin plugin, String code, String template, Sound sound) {
        String pluginName = plugin.getName();
        QueryBuilder<Template, Integer> queryBuilder = templateIntegerDao.queryBuilder();

        try {
            queryBuilder.where().eq("code", code).and().eq("plugin", pluginName); //TODO cache query
            Template exist = templateIntegerDao.queryForFirst(queryBuilder.prepare());
            if (exist == null) {
                exist = new Template(template, pluginName, code);
            }
            exist.setSound(sound);
            exist.setTemplate(template);
            templateIntegerDao.createOrUpdate(exist);
            return exist;
        } catch (SQLException e) {
            logSQLError(e);
            return null;
        }
    }

    /**
     * Shows the notification to all recipients.
     * @param notification the notification to show.
     */
    public void showNotification(Notification notification, boolean save) {
        Player recipient = null;
        if (notification.hasRecipient()) {
            String text = notification.getTemplate().compile(notification.getData());
            recipient = Bukkit.getPlayer(notification.getPlayer());
            if (!afk.isPlayerAFK(recipient)) {
                recipient.sendMessage(text);
                notification.playerRead();
                recipient.playSound(recipient.getLocation(), notification.getTemplate().getSound(), 1f, 1f);
            }
        }
        if (notification.isGlobal()) {
            if (recipient == null || !afk.isPlayerAFK(recipient)) {
                String text = notification.getBroadcastTemplate().compile(notification.getData());
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player == recipient) {
                        continue;
                    }
                    player.sendMessage(text);
                    player.playSound(player.getLocation(), notification.getBroadcastTemplate().getSound(), 1f, 1f);
                }
            }
        }
        if (save) {
            saveNotification(notification);
        }
    }

    /**
     * Saves any changes made to the given notification
     * @param n the notification to update in the database.
     */
    @Override
    public void saveNotification(Notification n) {
        try {
            notificationIntegerDao.createOrUpdate(n);
        } catch (SQLException e) {
            logSQLError(e);
        }
    }

    @Override
    public SummaryManager getSummaryManager() {
        return this.summaryManager;
    }

    // default visibility because no outside class should access the database directly
    Dao<Notification, Integer> getNotificationDao() {
        return notificationIntegerDao;
    }

    Dao<Template, Integer> getTemplateDao() {
        return templateIntegerDao;
    }

    /**
     * Convenience method to log an SQL error
     * @param e the error
     */
    static void logSQLError(SQLException e) {
        log.log(Level.SEVERE, "SQL Error", e);
    }
}

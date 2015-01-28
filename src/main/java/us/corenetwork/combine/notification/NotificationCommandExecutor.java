package us.corenetwork.combine.notification;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import us.corenetwork.combine.CombinePlugin;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NotificationCommandExecutor implements CommandExecutor {
    private SimpleCombineNotifications notifications;

    public NotificationCommandExecutor(SimpleCombineNotifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length == 0) { // summary
                notifications.getSummaryManager().sendSummary(player);
            }
            if (strings.length > 1) {
                if (strings[0].equalsIgnoreCase("check")) {
                    try {
                        UpdateBuilder<Notification, Integer> updateBuilder = notifications.getNotificationDao().updateBuilder();
                        Where<Notification, Integer> where = updateBuilder.updateColumnValue("read", System.currentTimeMillis()/1000).where();
                        for (int i = 1; i < strings.length; i++) {
                            where.eq("template_id", Integer.valueOf(strings[i]));
                        }
                        where.or(strings.length - 1);
                        where.and().eq("player", player.getUniqueId());
                        updateBuilder.update();
                        notifications.getSummaryManager().sendSummary(player);
                    } catch (SQLException e) {
                        SimpleCombineNotifications.logSQLError(e);
                    }

                }
                if (strings[0].equalsIgnoreCase("expand")) {
                    try {
                        player.sendMessage(ChatColor.YELLOW + "Notifications");
                        player.sendMessage(ChatColor.YELLOW + "-------------");
                        QueryBuilder<Notification, Integer> query = notifications.getNotificationDao().queryBuilder();
                        Where<Notification, Integer> where = query.where();
                        for (int i = 1; i < strings.length; i++) {
                            where.eq("template_id", Integer.valueOf(strings[i]));
                        }
                        where.or(strings.length - 1);
                        where.and().eq("player", player.getUniqueId());
                        query.orderBy("read", false);
                        query.query().forEach(notification -> {
                            FancyMessage message = new FancyMessage(notification.compileForPlayer())
                                    .then(" [x]")
                                    .tooltip("Click to check off")
                                    .command("/inbox check-notification " + notification.getId());
                            message.send(player);
                        });
                    } catch (SQLException e) {
                        SimpleCombineNotifications.logSQLError(e);
                    }
                }
            }
        } else {
            commandSender.sendMessage("Only players can receive notifications");
            //TODO accept notifications for virtual user '0' later
            return true;
        }
        return false;
    }
}

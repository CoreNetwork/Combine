package us.corenetwork.combine.notification;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NotificationCommandExecutor implements CommandExecutor {
    private SimpleCombineNotifications notifications;

    public NotificationCommandExecutor(SimpleCombineNotifications notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            if (strings.length == 0) { // summary
                notifications.getSummaryManager().sendSummary((Player) commandSender);
            }
        } else {
            commandSender.sendMessage("Only players can receive notifications");
            //TODO accept notifications for virtual user '0' later
            return true;
        }
        return false;
    }
}

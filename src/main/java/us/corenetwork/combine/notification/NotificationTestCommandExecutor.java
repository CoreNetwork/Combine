package us.corenetwork.combine.notification;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import us.corenetwork.combine.CombinePlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationTestCommandExecutor implements CommandExecutor {

    private Template template;
    private CombineNotifications notifications;
    private SummaryGenerator generator;

    private class PokeGenerator implements SummaryGenerator {

        @Override
        public String generateSummary(List<Notification> notifications) {
            if (notifications.size() == 1) {
                Notification notification = notifications.get(0);
                return notification.getTemplate().compile(notification.getData());
            }
            if (notifications.size() == 0) {
                return null;
            }
            return "You were poked " + notifications.size() + " times.";
        }

        @Override
        public Set<Template> getApplicableTemplates() {
            return new HashSet<Template>() {{ add(template); }};
        }
    }

    public NotificationTestCommandExecutor(CombineNotifications instance) {
        template = instance.registerTemplate(CombinePlugin.instance, "poke", "<Poker> poked you", Sound.STEP_WOOD);
        notifications = instance;
        generator = new PokeGenerator();
        notifications.getSummaryManager().registerGenerator(generator);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String playerName = strings[0];
        Notification notification = new Notification(Bukkit.getOfflinePlayer(playerName), template);
        notification.getData().put("Poker", commandSender.getName());
        notifications.createNotification(notification);
        return true;
    }
}

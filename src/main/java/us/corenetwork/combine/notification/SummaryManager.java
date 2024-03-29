package us.corenetwork.combine.notification;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SummaryManager {
    private SimpleCombineNotifications notifications;
    private LinkedHashSet<SummaryGenerator> generators = new LinkedHashSet<SummaryGenerator>();

    public SummaryManager(SimpleCombineNotifications notifications) {
        this.notifications = notifications;
    }


    public void sendSummary(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Notifications");
        player.sendMessage(ChatColor.YELLOW + "-------------");

        try {
            for (SummaryGenerator generator : generators) {
                QueryBuilder<Notification, Integer> queryBuilder = notifications.getNotificationDao().queryBuilder();
                Where<Notification, Integer> where = queryBuilder.where().isNull("read").and();
                Set<Template> applicableTemplates = generator.getApplicableTemplates();
                for (Template template : applicableTemplates) {
                    where.eq("template_id", notifications.getTemplateDao().extractId(template));
                }
                where.or(applicableTemplates.size());
                queryBuilder.orderBy("time", false);
                List<Notification> notificationList = notifications.getNotificationDao().query(queryBuilder.prepare());
                String summary = generator.generateSummary(notificationList);
                if (summary == null) {
                    continue;
                }

                StringBuilder command = new StringBuilder();
                for (Template template : generator.getApplicableTemplates()) {
                    command.append(' ');
                    command.append(template.getId());
                }

                FancyMessage message = new FancyMessage()
                        .text(summary)
                        .command("/inbox expand" + command.toString())
                        .tooltip("Click to expand")
                        .then(" [x]")
                        .tooltip("Click to check off")
                        .command("/inbox check" + command.toString());
                message.send(player);
            }
            player.sendMessage(ChatColor.YELLOW + "Click on entries to expand them.");
            // TODO order summaries by time
        } catch (SQLException e) {
            SimpleCombineNotifications.logSQLError(e);
        }
    }

    public boolean registerGenerator(SummaryGenerator summaryGenerator) {
        return generators.add(summaryGenerator);
    }
}

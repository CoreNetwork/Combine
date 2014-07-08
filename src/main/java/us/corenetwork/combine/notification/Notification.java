package us.corenetwork.combine.notification;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = "notifications")
/**
 * Represents a notification in the data model. The usual way you would create one would be:
 * <pre>
 *     Notification n = new Notification("Steve", template);
 *     n.getData().put("Items", "3");
 *     notifications.createNotification(n);
 * </pre>
 */
public class Notification {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = true)
    private UUID player;
    @DatabaseField(foreign = true, foreignAutoCreate = true, canBeNull = true)
    private Template template;
    @DatabaseField(foreign = true, foreignAutoCreate = true, canBeNull = true)
    private Template broadcastTemplate;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private HashMap<String, String> data = new HashMap<String, String>();
    @DatabaseField
    private long time;
    @DatabaseField(foreign = true)
    private List<PlayerReadState> readStates = new ArrayList<PlayerReadState>();
    @DatabaseField
    private boolean invalid = false;

    /**
     * Empty constructor for ORMLite
     */
    private Notification() {
    }

    /**
     * Constructs a notification with a recipient
     * @param player the recipient of the notification
     * @param template the template to display to the recipient
     */
    public Notification(Player player, Template template) {
        this(player.getUniqueId(), template);
    }

    /**
     * Constructs a notification
     * @param player the recipient of the notification
     * @param template the template to display to the recipient
     */
    public Notification(UUID player, Template template) {
        this.player = player;
        this.template = template;
        this.time = System.currentTimeMillis() / 1000;
    }

    /**
     * Constructs a global notification with a recipient. The recipient will get a different message using the given
     * recipientTemplate, all other players will get a message using the broadcastTemplate.<br/>
     * The same data is used for both templates.
     * @param player the recipient of the notification
     * @param recipientTemplate the template to display to the recipient
     * @param broadcastTemplate the template to display to other players
     */
    public Notification(UUID player, Template recipientTemplate, Template broadcastTemplate) {
        this(player, recipientTemplate);
        this.broadcastTemplate = broadcastTemplate;
    }

    /**
     * Constructs a global notification with a recipient. The recipient will get a different message using the given
     * recipientTemplate, all other players will get a message using the broadcastTemplate.<br/>
     * The same data is used for both templates.
     * @param player the recipient of the notification
     * @param recipientTemplate the template to display to the recipient
     * @param broadcastTemplate the template to display to other players
     */
    public Notification(Player player, Template recipientTemplate, Template broadcastTemplate) {
        this(player.getUniqueId(), recipientTemplate, broadcastTemplate);
    }

    /**
     * Constructs a global notification. All players that are online will receive this notification.
     * @param broadcastTemplate the template to display to all players
     */
    public Notification(Template broadcastTemplate) {
        this((UUID) null, null, broadcastTemplate);
    }

    /**
     * @return the recipient of this notification
     */
    public UUID getPlayer() {
        return player;
    }

    /**
     * @return the template used to show the notification to the recipient
     */
    public Template getTemplate() {
        return template;
    }


    /**
     * @return the template used to broadcast the notification to other players
     */
    public Template getBroadcastTemplate() {
        return broadcastTemplate;
    }

    /**
     * @return map of the variable assignments for the template.
     */
    public HashMap<String, String> getData() {
        return data;
    }

    /**
     * @return the time this notification was sent in UNIX epoch seconds.
     */
    public long getTime() {
        return time;
    }

    /**
     * @param player UUID of the player
     * @return if the given player has read the notification
     */
    public boolean hasPlayerRead(UUID player) {
        for (PlayerReadState state : readStates) {
            if (state.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Marks the notification as read by the given player
     * @param player the player that has read the notification
     */
    public void playerRead(Player player) {
        readStates.add(new PlayerReadState(player.getUniqueId(), System.currentTimeMillis() / 1000));
    }

    /**
     * @return if the recipient has read the notification
     */
    public boolean hasPlayerRead() {
        return hasPlayerRead(getPlayer());
    }

    /**
     * @return if the notification's context has become invalid. Invalid notifications are not shown but kept in the database for consistency reasons.
     */
    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Sets the invalid state of the notification.
     * @param invalid wether or not the notification is invalid
     */
    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    /**
     * @return if the notification is addressed to all players
     */
    public boolean isGlobal() {
        return broadcastTemplate != null;
    }

    /**
     * @return if the notification is addressed to a single player
     */
    public boolean hasRecipient() {
        return player != null;
    }

    /**
     * @return if the notification is <i>only</i> addressed to all players
     */
    public boolean isFullyGlobal() {
        return isGlobal() && !hasRecipient();
    }
}

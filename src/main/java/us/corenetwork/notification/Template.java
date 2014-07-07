package us.corenetwork.notification;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.HashMap;
import java.util.Map;

@DatabaseTable(tableName = "templates")
/**
 * Represents a template used in a Notification. Create like this:
 * <pre>
 *     Template greetingTemplate = notifications.registerTemplate(myPlugin, "greeting", "Hello, <Player>");
 * </pre>
 */
public class Template {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String template;
    @DatabaseField
    private String plugin;
    @DatabaseField
    private String code;

    /**
     * Empty constructor for ORMLite
     */
    private Template() {
    }

    /**
     * Creates a template instance. Please don't create one yourself; it confuses the system.<br/>
     *
     * Please use
     * {@link SimpleCoreNotifications#registerTemplate(org.bukkit.plugin.Plugin, String, String) registerTemplate}
     * to create an instance.
     * @param template the template that is displayed to the user
     * @param plugin the name of the plugin that registered the template
     * @param code a codename for the template. This shouldn't be changed after you register a template.
     */
    Template(String template, String plugin, String code) {
        this.template = template;
        this.plugin = plugin;
        this.code = code;
    }

    /**
     * @return the template string
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @return the plugin name of the template
     */
    public String getPlugin() {
        return plugin;
    }

    /**
     * @return the code for the template
     */
    public String getCode() {
        return code;
    }

    /**
     * @param variables a map of variable assignments.
     * @return a String where all given variables have been inserted into the template
     */
    public String compile(HashMap<String, String> variables) {
        String repl = template;
        for (Map.Entry<String, String> e : variables.entrySet()) {
            repl = repl.replaceAll("<" + e.getKey() + ">", e.getValue());
        }
        return repl;
    }

    /**
     * Updates the template value. Should only be called internally.
     * @param template the new template
     */
    void setTemplate(String template) {
        this.template = template;
    }
}

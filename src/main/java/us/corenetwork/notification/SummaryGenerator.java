package us.corenetwork.notification;

import java.util.List;
import java.util.Set;

public interface SummaryGenerator {
    public String generateSummary(List<Notification> notifications);

    public Set<Template> getApplicableTemplates();
}

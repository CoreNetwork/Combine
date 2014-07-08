package us.corenetwork.combine.notification;

import java.util.List;
import java.util.Set;

public interface SummaryGenerator {
    public String generateSummary(List<Notification> notifications);

    public Set<Template> getApplicableTemplates();
}

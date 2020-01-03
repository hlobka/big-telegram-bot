package telegram.bot.data.chat;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ChatPropertiesReader {
    private final Properties properties;

    public ChatPropertiesReader(Properties properties) {
        this.properties = properties;
    }

    public String getChatName() {
        return properties.getProperty("chat.name");
    }

    public String getJiraType() {
        return properties.getProperty("chat.atlassian.jira.typeId");
    }

    public ChatFilter getFilter() {
        return new ChatFilter(
            getPropertyAsBool("chat.filter.isActive"),
            getPropertyAsInt("chat.filter.delayInMinutes"),
            properties.getProperty("chat.filter.regexp")
        );
    }

    public long getChatId() {
        return Long.parseLong(properties.getProperty("chat.id"));
    }

    public List<String> getJenkinsIds() {
        return getPropertyAsList("chat.jenkins.ids");
    }

    public List<String> getJenkinsIdsForAllStatuses() {
        return getPropertyAsList("chat.jenkins.for_all_statuses.ids");
    }

    public List<String> getUpsourceIds() {
        return getPropertyAsList("chat.upsource.ids");
    }

    public List<String> getJiraIds() {
        return getPropertyAsList("chat.jira.project.keyIds");
    }

    public Boolean isEstimationRequired() {
        return getPropertyAsBool("chat.config.isEstimationRequired");
    }

    public Boolean isEstimationRequiredExcludeBugs() {
        return getPropertyAsBool("chat.config.isEstimationRequiredExcludeBugs");
    }

    public Boolean isMainGeneralChat() {
        return getPropertyAsBool("chat.config.isMainGeneral");
    }

    public Boolean isGeneralChat() {
        return getPropertyAsBool("chat.config.isGeneral");
    }

    public Boolean isReportChat() {
        return getPropertyAsBool("chat.config.isReport");
    }

    public Boolean isSpamChat() {
        return getPropertyAsBool("chat.config.isSpam");
    }

    private Integer getPropertyAsInt(String property) {
        return Integer.valueOf(properties.getProperty(property));
    }

    private Long getPropertyAsLong(String property) {
        return Long.valueOf(properties.getProperty(property));
    }

    private Boolean getPropertyAsBool(String property) {
        return "true".equalsIgnoreCase(properties.getProperty(property));
    }

    private List<String> getPropertyAsList(String property) {
        return Arrays.stream(properties.getProperty(property, "").split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}

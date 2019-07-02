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

    private Boolean getPropertyAsBool(String property) {
        return properties.getProperty(property).equalsIgnoreCase("true");
    }

    private List<String> getPropertyAsList(String property) {
        return Arrays.stream(properties.getProperty(property, "").split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}

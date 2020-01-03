package telegram.bot.data.chat;

import lombok.Data;
import telegram.bot.data.jira.FavoriteJqlRules;

import java.util.List;

@Data
public class ChatData {
    private final long chatId;
    private final String chatName;
    private final List<String> jenkinsIds;
    private final List<String> jenkinsIdsForAllStatuses;
    private final List<String> upsourceIds;
    private final List<String> jiraProjectKeyIds;
    private final Boolean isEstimationRequired;
    private final Boolean isMainGeneral;
    private final Boolean isGeneral;
    private final Boolean isReport;
    private final Boolean isSpam;
    private final FavoriteJqlRules jiraConfig;
    private final ChatFilter chatFilter;

}

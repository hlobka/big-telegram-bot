package telegram.bot.checker.workFlow;

import telegram.bot.data.chat.ChatData;

import java.util.List;

public interface ChatChecker {
    /**
     * Should check and return message of check result in MarkDown format
     * @param chatData is telegram chat data
     * @return messages of check result in MarkDown format
     */
    List<String> check(ChatData chatData);

    /**
     * Need to check, is this checker available for getting check result
     * @param chatData is telegram chat data
     * @return status
     */
    Boolean isAccessibleToCheck(ChatData chatData);
}

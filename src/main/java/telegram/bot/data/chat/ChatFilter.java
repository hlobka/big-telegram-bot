package telegram.bot.data.chat;

import lombok.Data;

@Data
public class ChatFilter {
    private final Boolean isActive;
    private final Integer delayInMinutes;
    private final String regexp;
}

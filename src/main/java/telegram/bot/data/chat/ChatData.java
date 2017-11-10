package telegram.bot.data.chat;

import lombok.Data;

import java.util.List;

@Data
public class ChatData {
    private final long chatId;
    private final List<String> jenkinsIds;
}

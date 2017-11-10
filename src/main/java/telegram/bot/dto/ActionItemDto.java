package telegram.bot.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ActionItemDto implements Serializable{
    private final String date;
    private final String value;
    private final long chatId;
}

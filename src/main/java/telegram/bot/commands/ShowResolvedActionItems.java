package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.dto.ActionItemDto;

import java.util.*;

import static telegram.bot.data.Common.RESOLVED_ACTION_ITEMS;
import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;

public class ShowResolvedActionItems implements Command {

    private boolean showAll;

    public ShowResolvedActionItems(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Map<Integer, ActionItemDto> actionItems = SharedObject.loadMap(RESOLVED_ACTION_ITEMS, new HashMap<Integer, ActionItemDto>());
        List<String> messages = new ArrayList<>();
        StringBuilder s = new StringBuilder("Resolved Action items: \n");
        messages.add(s.toString());
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        boolean isBigGroup = BIG_GENERAL_GROUP_IDS.contains(chatId);
        for (Map.Entry<Integer, ActionItemDto> entry : actionItems.entrySet()) {
            ActionItemDto actionItemDto = entry.getValue();
            if (!showAll) {
                long actionItemChatId = actionItemDto.getChatId();
                if(isBigGroup){
                    if(!BIG_GENERAL_GROUP_IDS.contains(actionItemChatId)){
                        continue;
                    }
                } else if (actionItemChatId != chatId) {
                    continue;
                }
            }
            String date = actionItemDto.getDate();
            s = new StringBuilder();
            String actionItem = actionItemDto.getValue().replaceAll("#(AI|ai)", "<b>AI: </b>")
                .replaceAll("<", "")
                .replaceAll(">", "");
            s.append("    • ").append(date).append(" <pre>").append(actionItem).append("</pre>\n");
            messages.add(s.toString());
        }
        return new Pair<>(ParseMode.HTML, messages);
    }
}

package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.dto.ActionItemDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static telegram.bot.data.Common.ACTION_ITEMS2;
import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;

public class ShowActionItems implements Command {

    private boolean showAll;

    public ShowActionItems(boolean showAll) {

        this.showAll = showAll;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Map<Integer, ActionItemDto> actionItems = SharedObject.loadMap(ACTION_ITEMS2, new HashMap<Integer, ActionItemDto>());
        StringBuilder s = new StringBuilder("Action items: \n");
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        boolean isBigGroup = BIG_GENERAL_GROUP_IDS.contains(chatId);
        for (Map.Entry<Integer, ActionItemDto> entry : actionItems.entrySet()) {
            ActionItemDto actionItemDto = entry.getValue();
            if (!isNeedShowActionItem(actionItemDto, isBigGroup, chatId)) {
                continue;
            }
            String date = actionItemDto.getDate();
            String actionItem = actionItemDto.getValue().replaceAll("#(AI|ai)", "<b>AI: </b>")
                .replaceAll("\\[", "")
                .replaceAll("]", "")
//                .replaceAll("\\)", "")
//                .replaceAll("\\(", "")
                .replaceAll("<", "")
                .replaceAll(">", "");
            s.append("    • ").append(date).append(" <pre>").append(actionItem).append("</pre>\n");
        }
        String s1 = s.toString();
        return new Pair<>(ParseMode.HTML, Collections.singletonList(s1));
    }

    private boolean isNeedShowActionItem(ActionItemDto actionItemDto, boolean isBigGroup, Long chatId) {
        long actionItemChatId = actionItemDto.getChatId();
        boolean isActionItemFromThisChat = actionItemChatId == chatId;
        if (isActionItemFromThisChat) {
            return true;
        }
        if (isBigGroup) {
            boolean isActionItemInBigGroup = BIG_GENERAL_GROUP_IDS.contains(actionItemChatId);
            return showAll && isActionItemInBigGroup;
        }
        return showAll;
    }
}

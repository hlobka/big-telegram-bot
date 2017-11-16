package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;
import telegram.bot.dto.ActionItemDto;

import java.util.HashMap;
import java.util.Map;

import static telegram.bot.data.Common.ACTION_ITEMS2;
import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;

public class ConfigureActionItems implements Command {

    private final boolean showAll;

    public ConfigureActionItems(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public Pair<ParseMode, String> run(Update update, String values) {
        Map<Integer, ActionItemDto> actionItems = SharedObject.loadMap(ACTION_ITEMS2, new HashMap<Integer, ActionItemDto>());
        String s = "Action items: \n";
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        boolean isBigGroup = BIG_GENERAL_GROUP_IDS.contains(chatId);
        for (Map.Entry<Integer, ActionItemDto> entry : actionItems.entrySet()) {
            ActionItemDto actionItemDto = entry.getValue();
            if (!isNeedShowActionItem(actionItemDto, isBigGroup, chatId)) {
                continue;
            }
            int hashCode = entry.getKey();
            String date = entry.getValue().getDate().replaceAll(":\\d\\d$", "");
            String actionItem = entry.getValue().getValue().replaceAll("#(AI|ai)", "<b>AI: </b>")
                .replaceAll("<", "")
                .replaceAll(">", "");
            s += "/resolveAI__" + hashCode +" "+ date + " : [" + actionItem +"]\n";
        }
        return new Pair<>(ParseMode.HTML, s);
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

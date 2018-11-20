package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.data.Common;
import telegram.bot.dto.ActionItemDto;
import telegram.bot.helper.ActionItemsHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfigureActionItems implements Command {

    private final boolean showAll;

    public ConfigureActionItems(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Map<Integer, ActionItemDto> actionItems = ActionItemsHelper.unresolved.loadActionItems();
        String s = "Action items: \n";
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        boolean isBigGroup = Common.data.isGeneralChat(chatId);
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
        return new Pair<>(ParseMode.HTML, Collections.singletonList(s));
    }


    private boolean isNeedShowActionItem(ActionItemDto actionItemDto, boolean isBigGroup, Long chatId) {
        long actionItemChatId = actionItemDto.getChatId();
        boolean isActionItemFromThisChat = actionItemChatId == chatId;
        if (isActionItemFromThisChat) {
            return true;
        }
        if (isBigGroup) {
            boolean isActionItemInBigGroup = Common.data.isGeneralChat(actionItemChatId);
            return showAll && isActionItemInBigGroup;
        }
        return showAll;
    }
}

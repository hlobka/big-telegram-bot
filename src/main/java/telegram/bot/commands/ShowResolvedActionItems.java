package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.data.Common;
import telegram.bot.dto.ActionItemDto;
import telegram.bot.helper.ActionItemsHelper;
import telegram.bot.helper.MessageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShowResolvedActionItems implements Command {

    private boolean showAll;

    public ShowResolvedActionItems(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Map<Integer, ActionItemDto> actionItems = ActionItemsHelper.resolved.loadActionItems();
        List<String> messages = new ArrayList<>();
        StringBuilder s = new StringBuilder("Resolved Action items: \n");
        messages.add(s.toString());
        Message message = MessageHelper.getAnyMessage(update);
        Long chatId = message.chat().id();
        boolean isBigGroup = Common.data.isGeneralChat(chatId);
        for (Map.Entry<Integer, ActionItemDto> entry : actionItems.entrySet()) {
            ActionItemDto actionItemDto = entry.getValue();
            if (!showAll) {
                long actionItemChatId = actionItemDto.getChatId();
                if(isBigGroup){
                    if(!Common.data.isGeneralChat(actionItemChatId)){
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

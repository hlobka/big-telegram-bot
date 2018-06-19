package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.dto.ActionItemDto;
import telegram.bot.helper.ActionItemsHelper;
import java.util.*;

public class ClearActionItem implements Command {

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        ActionItemsHelper unresolvedAiHelper = ActionItemsHelper.unresolved;
        ActionItemsHelper resolvedAiHelper = ActionItemsHelper.resolved;
        HashMap<Integer, ActionItemDto> actionItems = unresolvedAiHelper.loadActionItems();
        HashMap<Integer, ActionItemDto> resolvedItems = resolvedAiHelper.loadActionItems();
        Integer key = values.isEmpty()?0:Integer.parseInt(values);
        if (actionItems.containsKey(key)) {
            ActionItemDto remove = actionItems.remove(key);
            resolvedItems.put(key, remove);
            unresolvedAiHelper.saveActionItems(actionItems);
            resolvedAiHelper.saveActionItems(resolvedItems);
            String message = String.format("Action item successfully resolved. <pre>%s</pre>\nTap /showActionItems to update list", remove.getValue());
            return new Pair<>(ParseMode.HTML, Collections.singletonList(message));
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Action item does not exist. \nTap /showActionItems to update list"));
    }
}

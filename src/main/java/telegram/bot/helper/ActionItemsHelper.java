package telegram.bot.helper;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.file.SharedObject;
import telegram.bot.dto.ActionItemDto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static telegram.bot.data.Common.ACTION_ITEMS2;
import static telegram.bot.data.Common.RESOLVED_ACTION_ITEMS;

public class ActionItemsHelper {
    public static ActionItemsHelper unresolved = new ActionItemsHelper(ACTION_ITEMS2);
    public static ActionItemsHelper resolved = new ActionItemsHelper(RESOLVED_ACTION_ITEMS);
    private final String actionItemsKey;

    public ActionItemsHelper(String actionItemsKey) {
        this.actionItemsKey = actionItemsKey;
    }

    public int saveActionItem(String text, long chatId) {
        return saveActionItem(text, chatId, 0);
    }

    public int saveActionItem(String text, long chatId, Integer uniqueKey) {
        HashMap<Integer, ActionItemDto> actionItems = loadActionItems();
        String date = new SimpleDateFormat("dd.MM/HH:mm:ss").format(Calendar.getInstance().getTime());
        int key = Math.abs((date + uniqueKey).hashCode());
        actionItems.put(key, new ActionItemDto(date, text, chatId));
        saveActionItems(actionItems);
        return key;
    }

    public ActionItemDto readActionItem(int key) {
        HashMap<Integer, ActionItemDto> actionItems = loadActionItems();
        return actionItems.get(key);
    }

    public void removeActionItem(int key) {
        HashMap<Integer, ActionItemDto> actionItems = loadActionItems();
        actionItems.remove(key);
        saveActionItems(actionItems);
    }

    public void saveActionItems(HashMap<Integer, ActionItemDto> actionItems) {
        SharedObject.save(actionItemsKey, actionItems);
    }

    public HashMap<Integer, ActionItemDto> loadActionItems() {
        return SharedObject.loadMap(actionItemsKey, new HashMap<>());
    }

    public void askForDeadLine(TelegramBot bot, int actionItemKey, Message message) {
        Long chatId = message.chat().id();
        String text = "Хотите установить дедлайн?";
        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Sure").callbackData("choose_aiDl_"+actionItemKey),
                new InlineKeyboardButton("No").callbackData("avoid_ai_dead_line_"+actionItemKey),
            }));
        bot.execute(request);
    }
}

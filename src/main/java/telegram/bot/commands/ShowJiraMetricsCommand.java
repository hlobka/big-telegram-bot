package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import javafx.util.Pair;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShowJiraMetricsCommand implements Command {
    public static final String SHOW_JIRA_STATISTIC = "show_jira_statistic";
    private final TelegramBot bot;

    public ShowJiraMetricsCommand(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        if(!Common.data.telegramUserIdsWithGeneralAccess.contains(message.from().id())){
            BotHelper.sendMessage(bot, message.chat().id(), "You cannot have access for this operation", ParseMode.Markdown);
            return new Pair<>(ParseMode.HTML, Collections.singletonList(""));
        }
        List<String> jiraProjectKeyIds = new ArrayList<>();
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            jiraProjectKeyIds.addAll(generalChat.getJiraProjectKeyIds());
        }
        jiraProjectKeyIds = jiraProjectKeyIds.stream().distinct().collect(Collectors.toList());

        InlineKeyboardButton[] buttons = getInlineKeyboardButtons(jiraProjectKeyIds);
        sendMessage(message.chat().id(), "Choose project: ", buttons);

        return new Pair<>(ParseMode.HTML, Collections.singletonList(""));
    }

    private void sendMessage(long groupId, String message, InlineKeyboardButton[] buttons) {
        SendMessage request = new SendMessage(groupId, message)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(buttons));
        bot.execute(request);
    }

    private static InlineKeyboardButton[] getInlineKeyboardButtons(List<String> jiraProjectKeyIds) {
        InlineKeyboardButton[] result = new InlineKeyboardButton[jiraProjectKeyIds.size()];
        for (int i = 0; i < jiraProjectKeyIds.size(); i++) {
            String jiraId = jiraProjectKeyIds.get(i);
            String callbackId = SHOW_JIRA_STATISTIC + ":" + jiraId;
            result[i] = new InlineKeyboardButton(jiraId).callbackData(callbackId);
        }
        return result;
    }
}

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
import telegram.bot.helper.MessageHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShowJiraMetricsCommand implements Command {
    public static final String SHOW_JIRA_STATISTIC = "show_sprint_jira_statistic";
    public static final String SHOW_JIRA_STATISTIC_FOR_ALL_PERIOD = "show_full_jira_statistic";
    private final TelegramBot bot;
    private final String command;

    public ShowJiraMetricsCommand(TelegramBot bot, String command) {
        this.bot = bot;
        this.command = command;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = MessageHelper.getAnyMessage(update);
        Long userId = message.from().id();
        Long chatId = message.chat().id();
        if(!Common.data.telegramUserIdsWithGeneralAccess.contains(userId)){
            return new Pair<>(ParseMode.HTML, Collections.singletonList("You cannot have access for this operation"));
        }
        if(!values.isEmpty()){
            showJiraMetricsByProjectId(update, values);
        } else {
            showAllProjects(chatId);
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList(""));
    }

    private void showJiraMetricsByProjectId(Update update, String projectId) {
        boolean forAllPeriod = command.equals(SHOW_JIRA_STATISTIC_FOR_ALL_PERIOD);
        new ShowJiraMetricsByProjectIdCommand(bot, forAllPeriod).run(update, projectId);
    }

    private void showAllProjects(Long chatId) {
        List<String> jiraProjectKeyIds = new ArrayList<>();
        for (ChatData generalChat : Common.data.getGeneralChats()) {
            jiraProjectKeyIds.addAll(generalChat.getJiraProjectKeyIds());
        }
        jiraProjectKeyIds = jiraProjectKeyIds.stream().distinct().collect(Collectors.toList());

        InlineKeyboardButton[] buttons = getInlineKeyboardButtons(jiraProjectKeyIds);
        sendMessage(chatId, "Choose project: ", buttons);
    }

    private void sendMessage(long groupId, String message, InlineKeyboardButton[] buttons) {
        SendMessage request = new SendMessage(groupId, message)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(new InlineKeyboardMarkup(buttons));
        bot.execute(request);
    }

    private InlineKeyboardButton[] getInlineKeyboardButtons(List<String> jiraProjectKeyIds) {
        InlineKeyboardButton[] result = new InlineKeyboardButton[jiraProjectKeyIds.size()];
        for (int i = 0; i < jiraProjectKeyIds.size(); i++) {
            String jiraId = jiraProjectKeyIds.get(i);
            String callbackId = command + ":" + jiraId;
            result[i] = new InlineKeyboardButton(jiraId).callbackData(callbackId);
        }
        return result;
    }
}

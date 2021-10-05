package telegram.bot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.checker.UpsourceChecker;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.helper.MessageHelper;

import java.util.Collections;
import java.util.List;

public class UpdateUpsourceViewResult implements Command {
    private TelegramBot bot;

    public UpdateUpsourceViewResult(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = MessageHelper.getAnyMessage(update);
        if (message == null){
            message = update.callbackQuery().message();
        }
        LoginData jiraLoginData = Common.data.getChatData(message.chat().id()).getJiraConfig().getLoginData();
        UpsourceChecker.updateMessage(bot, values, message, jiraLoginData);
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Ok: "));
    }
}

package telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import telegram.bot.checker.UpsourceChecker;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.rules.ReLoginRule;
import telegram.bot.rules.Rules;

import java.io.IOException;
import java.util.Collections;

public class UpsourceCheckerMain {

    public static void main(String[] args) throws IOException {
        TelegramBot bot = new TelegramBot(Common.data.token);
        ChatData chatData = Common.data.getChatData("REPORT");
        new UpsourceChecker(bot).check(chatData);
    }
}

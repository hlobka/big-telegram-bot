package telegram;

import com.pengrad.telegrambot.TelegramBot;
import telegram.bot.checker.JiraChecker;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class JiraCheckerMain {
    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
        ChatData chatData = new ChatData(Common.TEST_FOR_BOT_GROUP_ID, null, null, Collections.singletonList("WILDFU"));
        new JiraChecker(bot, TimeUnit.MINUTES.toMillis(60)).check(chatData);
    }
}

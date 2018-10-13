package telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import external.ExternalJob;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;

import java.io.IOException;

public class ExternalJobRunnerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        TelegramBot bot = new TelegramBot(Common.data.token);
        for (ExternalJob externalJob : Common.EXTERNAL_JOBS) {
            externalJob.run(errorMsg->{
                for (Long chatId : Common.getChatIdList(externalJob.groupId)) {
                    BotHelper.sendMessage(bot, chatId, "```"+ errorMsg + "```", ParseMode.Markdown);
                }
            });
        }
    }
}

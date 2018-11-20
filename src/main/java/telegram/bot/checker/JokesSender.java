package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.logger.ConsoleLogger;
import helper.time.TimeHelper;
import joke.JokesProvider;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;

import java.util.concurrent.TimeUnit;

public class JokesSender extends Thread {
    private TelegramBot bot;

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
        new JokesSender(bot).check();
    }

    public JokesSender(TelegramBot bot) {
        this.bot = bot;
    }


    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                if (!sleepToNextCheck()) {
                    continue;
                }
            } catch (InterruptedException e) {
                ConsoleLogger.logErrorFor(this, e);
                Thread.interrupted();
                return;
            }
            check();
        }
    }

    private boolean sleepToNextCheck() throws InterruptedException {
        Long minutesUntilTargetHour = getMinutesUntilNextTargetHour();
        TimeUnit.MINUTES.sleep(minutesUntilTargetHour);
        return true;

    }

    private Long getMinutesUntilNextTargetHour() {
        Long morning = TimeHelper.getMinutesUntilTargetHour(9);
        Long lunch = TimeHelper.getMinutesUntilTargetHour(13);
        Long evening = TimeHelper.getMinutesUntilTargetHour(18);
        return Math.min(morning, Math.min(lunch, evening));
    }

    public void check() {
        String joke = new JokesProvider("anekdot.ru", "new%20anekdot").provideNextUniqueJoke(100);
        joke = BotHelper.clearForHtmlMessages(joke);
        for (ChatData spamChat : Common.data.getChatsFotSpam()) {
            BotHelper.sendMessage(bot, spamChat.getChatId(), joke, ParseMode.HTML);
        }
    }

}

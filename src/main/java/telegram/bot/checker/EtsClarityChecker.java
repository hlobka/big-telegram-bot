package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.string.StringHelper;
import telegram.bot.data.Common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EtsClarityChecker extends Thread {
    private TelegramBot bot;
    private long millis;

    public EtsClarityChecker(TelegramBot bot, long millis) throws URISyntaxException {
        this.bot = bot;
        this.millis = millis;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                long timeout = TimeUnit.MINUTES.toMillis(getTimeout());
                long oneMinuteInMilliseconds = TimeUnit.MINUTES.toMillis(1);
                long min = Math.max(oneMinuteInMilliseconds, Math.min(millis, timeout));
                if(min>0) {
                    TimeUnit.MILLISECONDS.sleep(min);
                }
                check();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void check() throws IOException {
        System.out.println("EtsClarityChecker::check");
        boolean isFriday = LocalDate.now().getDayOfWeek() == DayOfWeek.FRIDAY;
        if (isFriday) {
            Calendar calendar = Calendar.getInstance();
            int currentTimeInHours = calendar.get(Calendar.HOUR_OF_DAY);
            int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
            if (currentTimeInHours >= 10 && currentTimeInHours < 17) {
                int timeout = getTimeout();
                if (timeout > 0) {
                    sleep(timeout, TimeUnit.MINUTES);
                }
                sendNotification(Common.BIG_GENERAL_CHAT_ID);
                System.out.println(new Date().getTime() + "::EtsClarityChecker::TRUE; Hours: " + currentTimeInHours + "; Minutes: " + currentTimeInMinutes);
            }
        }
    }

    private int getTimeout() {
        Calendar calendar = Calendar.getInstance();
        int currentTimeInMinutes = calendar.get(Calendar.MINUTE);
        return 59 - currentTimeInMinutes;
    }

    private void sendNotification(long chatId) {
        String message = getMessage();
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false);
        bot.execute(request);
    }

    private String getMessage() {
        String message = null;
        try {
            message = StringHelper.getFileAsString("ets_clarity.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    private void sleep(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}

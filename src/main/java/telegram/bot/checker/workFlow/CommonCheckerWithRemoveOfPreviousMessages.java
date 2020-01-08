
package telegram.bot.checker.workFlow;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.response.SendResponse;
import helper.logger.ConsoleLogger;
import helper.time.TimeHelper;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static helper.logger.ConsoleLogger.logFor;

public class CommonCheckerWithRemoveOfPreviousMessages extends Thread {
    private final TelegramBot bot;
    private final long timeoutInMillis;
    private List<ChatChecker> chatCheckerList;
    private int numberOfAttempts = -1;
    private int maxNumberOfAttempts = 1;
    private int idleTimeoutMultiplier;
    private List<Message> messagesToRemove;

    public CommonCheckerWithRemoveOfPreviousMessages(TelegramBot bot, long timeoutInMillis) {
        super("CommonCheckerWithRemoveOfPreviousMessages " + timeoutInMillis);
        this.bot = bot;
        this.timeoutInMillis = timeoutInMillis;
        chatCheckerList = new ArrayList<>();
        messagesToRemove = new ArrayList<>();
    }

    public CommonCheckerWithRemoveOfPreviousMessages withChecker(ChatChecker chatChecker) {
        chatCheckerList.add(chatChecker);
        return this;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                sleepToNextCheck();
                if (TimeHelper.isWeekends() || TimeHelper.isNight()) {
                    numberOfAttempts = 0;
                    continue;
                }
                check();
                numberOfAttempts = 0;
            } catch (InterruptedException e) {
                ConsoleLogger.logErrorFor(this, e);
                if (numberOfAttempts++ > maxNumberOfAttempts) {
                    return;
                }
                ConsoleLogger.logError(e, String.format("will be rerun in %d minutes. Attempt: %d", TimeUnit.MILLISECONDS.toMinutes(getMillisToNextRun()), numberOfAttempts));
            }
        }
    }

    private void sleepToNextCheck() throws InterruptedException {
        long millisToNextRun = getMillisToNextRun();
        logFor(this, "sleepToNextCheck: " + TimeUnit.MILLISECONDS.toMinutes(millisToNextRun) + " minutes");
        TimeUnit.MILLISECONDS.sleep(millisToNextRun);
    }

    private long getMillisToNextRun() {
        long millis = (numberOfAttempts == -1) ? 1 : timeoutInMillis;
        if (TimeHelper.isWeekends() || TimeHelper.isNight()) {
            millis = millis * idleTimeoutMultiplier;
        }
        return millis;
    }

    public void check() {
        logFor(this, "check:start");
        removeMessages();
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            check(chatData);
        }
        logFor(this, "check:end");
    }

    public void check(ChatData chatData) {
        List<String> messages = new ArrayList<>();
        for (ChatChecker chatChecker : chatCheckerList) {
            if (chatChecker.isAccessibleToCheck(chatData)) {
                try {
                    messages.addAll(chatChecker.check(chatData));
                } catch (RuntimeException e) {
                    ConsoleLogger.logError(e, "chatChecker.check:for "+ chatData.getChatName());
                }
            }
        }
        messages = messages.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        for (String message : messages) {
            SendResponse sendResponse = BotHelper.sendMessage(bot, chatData.getChatId(), message, ParseMode.Markdown);
            messagesToRemove.add(sendResponse.message());
        }
    }

    private void removeMessages() {
        for (Message message : messagesToRemove) {
            BotHelper.removeMessage(bot, message);
        }
        messagesToRemove.clear();

    }

    public CommonCheckerWithRemoveOfPreviousMessages withIdleTimeoutMultiplier(int multiplier) {
        idleTimeoutMultiplier = multiplier;
        return this;
    }

    public CommonCheckerWithRemoveOfPreviousMessages withMaxNumberOfAttempts(int maxNumberOfAttempts) {
        this.maxNumberOfAttempts = maxNumberOfAttempts;
        return this;
    }
}

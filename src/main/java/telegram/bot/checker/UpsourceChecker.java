package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import javafx.util.Pair;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;
import upsource.ReviewState;
import upsource.UpsourceApi;
import upsource.dto.Review;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpsourceChecker extends Thread {
    private TelegramBot bot;
    public static final String TITLE = "Господа, ваши содевелоперы, ожидают фидбэка по ревью, Будьте бдительны, Не заставляйте их ждать!!!";

    public UpsourceChecker(TelegramBot bot) {
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
                e.printStackTrace();
                Thread.interrupted();
                return;
            }
            try {
                check();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private boolean sleepToNextCheck() throws InterruptedException {
        boolean isWeekends = TimeHelper.checkToDayIs(DayOfWeek.SUNDAY) || TimeHelper.checkToDayIs(DayOfWeek.SATURDAY);
        if (isWeekends) {
            TimeUnit.HOURS.sleep(1);
            return false;
        }
        Long minutesUntilTargetHour = getMinutesUntilNextTargetHour();
        TimeUnit.MINUTES.sleep(minutesUntilTargetHour);
        return true;

    }

    private Long getMinutesUntilNextTargetHour() {
        Long minutesUntilTargetHourForFirstPartOfDay = TimeHelper.getMinutesUntilTargetHour(10);
        Long minutesUntilTargetHourForSecondPartOfDay = TimeHelper.getMinutesUntilTargetHour(18);
        return Math.min(minutesUntilTargetHourForFirstPartOfDay, minutesUntilTargetHourForSecondPartOfDay);
    }

    private void check() throws IOException {
        System.out.println("UpsourceChecker::check");
        UpsourceApi upsourceApi = new UpsourceApi(Common.UPSOURCE.url, Common.UPSOURCE.login, Common.UPSOURCE.pass);
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            List<Pair<String, String>> messages = new ArrayList<>();
            for (String upsourceId : chatData.getUpsourceIds()) {
                String message = getUpsourceViewResult(upsourceApi, upsourceId);
                if (message.length() > 0) {
                    messages.add(new Pair<>(upsourceId, message));
                }
            }
            sendMessagesWithViewResult(chatData, messages);
        }
    }

    private void sendMessagesWithViewResult(ChatData chatData, List<Pair<String, String>> messages) {
        if (messages.size() == 1) {
            Pair<String, String> projectIdOnMessagePair = messages.get(0);
            sendMessageWithInlineBtns(chatData, TITLE + projectIdOnMessagePair.getValue(), projectIdOnMessagePair.getKey());
        } else if (messages.size() > 0) {
            BotHelper.sendMessage(bot, chatData.getChatId(), TITLE, ParseMode.Markdown);
            for (Pair<String, String> message : messages) {
                sendMessageWithInlineBtns(chatData, message.getValue(), message.getKey());
            }

        }
    }

    private static String getUpsourceViewResult(UpsourceApi upsourceApi, String upsourceId) throws IOException {
        String message = "";
        List<Review> reviews = upsourceApi.getProject(upsourceId)
            .getReviewsProvider(true)
            .withDuration(TimeUnit.DAYS.toMillis(1))
            .withState(ReviewState.OPEN)
            .withCompleteCount(0, CountCondition.MORE_THAN_OR_EQUALS)
            .getReviews();
        String format = "%n%1$-13s|%2$11s|%3$-15s|%4$5s|%5$3s";
        if (reviews.size() > 0) {
            message += "\n * " + upsourceId + " *";
            message += "\n```";
            message += "\n------------------------------------------------------";
            message += String.format(format, "Содевелопер", "Задача", "Ревью", "Готов", "Статус");
            message += "\n------------------------------------------------------";
        }
        for (Review review : reviews) {
            String createdBy = Common.UPSOURCE.userIdOnNameMap.get(review.createdBy());
            String issueId = StringHelper.getIssueIdFromSvnRevisionComment(review.title());
            String completedRate = review.completionRate().completedCount + "/" + review.completionRate().reviewersCount;
            message += String.format(format, createdBy, issueId, review.reviewId(), !review.discussionCounter().hasUnresolved, completedRate);
            review.title();
        }
        if (reviews.size() > 0) {
            message += "\n------------------------------------------------------";
            message += "\n```";
        }
        return message;
    }

    private void sendMessageWithInlineBtns(ChatData chatData, String message, String upsourceProjectId) {
        SendMessage request = new SendMessage(chatData.getChatId(), message)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(getReplyMarkup(upsourceProjectId));
        SendResponse execute = bot.execute(request);
    }

    public static void updateMessage(TelegramBot bot, String upsourceProjectId, Message message) {
        UpsourceApi upsourceApi = new UpsourceApi(Common.UPSOURCE.url, Common.UPSOURCE.login, Common.UPSOURCE.pass);
        String upsourceViewResult = null;
        try {
            upsourceViewResult = getUpsourceViewResult(upsourceApi, upsourceProjectId);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        updateMessage(bot, upsourceProjectId, message, upsourceViewResult);
    }

    private static void updateMessage(TelegramBot bot, String upsourceProjectId, Message message, String newMessage) {
        if(message.text().contains(TITLE)){
            newMessage = TITLE + newMessage;
        }
        try {
            EditMessageText request = new EditMessageText(message.chat().id(), message.messageId(), newMessage)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(getReplyMarkup(upsourceProjectId));
            bot.execute(request);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static InlineKeyboardMarkup getReplyMarkup(String upsourceProjectId) {
        return new InlineKeyboardMarkup(new InlineKeyboardButton[] {
            new InlineKeyboardButton("Обновить")
                .callbackData("update_upsource_checker_view_result_for:" + upsourceProjectId),
            new InlineKeyboardButton("Подробнее")
                .callbackData("show_upsource_checker_tabs_description")
        });
    }
}

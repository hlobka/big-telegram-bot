package telegram.bot.checker;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.string.StringHelper;
import helper.time.TimeHelper;
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
            String title = "Господа, ваши содевелоперы, ожидают фидбэка по ревью, Будьте бдительны, Не заставляйте их ждать!!!";
            List<String> messages = new ArrayList<>();
            for (String upsourceId : chatData.getUpsourceIds()) {
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
                    messages.add(message);
                }

            }
            if (messages.size() == 1) {
                BotHelper.sendMessage(bot, chatData.getChatId(), title + messages.get(0), ParseMode.Markdown);
            } else if (messages.size() > 0) {
                BotHelper.sendMessage(bot, chatData.getChatId(), title, ParseMode.Markdown);
                for (String s : messages) {
                    BotHelper.sendMessage(bot, chatData.getChatId(), s, ParseMode.Markdown);
                }

            }
        }
    }
}

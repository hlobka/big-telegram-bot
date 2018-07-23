package telegram.bot.checker;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static helper.logger.ConsoleLogger.log;

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
        Long minutesUntilTargetHour = getMinutesUntilNextTargetHour();
        TimeUnit.MINUTES.sleep(minutesUntilTargetHour);
        if (isWeekends()) {
            TimeUnit.MINUTES.sleep(1);
            return false;
        }
        return true;

    }

    private boolean isWeekends() {
        return TimeHelper.checkToDayIs(DayOfWeek.SUNDAY) || TimeHelper.checkToDayIs(DayOfWeek.SATURDAY);
    }

    private Long getMinutesUntilNextTargetHour() {
        Long minutesUntilTargetHourForFirstPartOfDay = TimeHelper.getMinutesUntilTargetHour(10);
        Long minutesUntilTargetHourForSecondPartOfDay = TimeHelper.getMinutesUntilTargetHour(18);
        return Math.min(minutesUntilTargetHourForFirstPartOfDay, minutesUntilTargetHourForSecondPartOfDay);
    }

    public void check() throws IOException {
        UpsourceApi upsourceApi = getUpsourceApi();
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            check(upsourceApi, chatData);
        }
    }

    public void check(ChatData chatData) throws IOException {
        UpsourceApi upsourceApi = getUpsourceApi();
        check(upsourceApi, chatData);
    }

    private static UpsourceApi getUpsourceApi() {
        return new UpsourceApi(Common.UPSOURCE.url, Common.UPSOURCE.login, Common.UPSOURCE.pass);
    }

    public void check(UpsourceApi upsourceApi, ChatData chatData) throws IOException {
        log(chatData.getUpsourceIds().toString());
        List<Pair<String, String>> messages = new ArrayList<>();
        for (String upsourceId : chatData.getUpsourceIds()) {
            String message = getUpsourceViewResult(upsourceApi, upsourceId);
            if (message.length() > 0) {
                messages.add(new Pair<>(upsourceId, message));
            }
        }
        sendMessagesWithViewResult(chatData, messages);
        log("UpsourceChecker::check:end");
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
        List<Review> reviews = upsourceApi.getProject(upsourceId)
            .getReviewsProvider(true)
//            .withDuration(TimeUnit.DAYS.toMillis(1))
            .withState(ReviewState.OPEN)
            .withCompleteCount(0, CountCondition.MORE_THAN_OR_EQUALS)
            .withReviewersCount(0, CountCondition.MORE_THAN)
            .getReviews().stream().sorted(Comparator.comparing((review) -> getMappedReviewerName(review).toLowerCase())).collect(Collectors.toList());

        JiraHelper jiraHelper = JiraHelper.getClient(Common.JIRA, true);
        List<Review> unVersionReviews = extractUnVersionReviews(reviews, jiraHelper);
        List<Review> abnormalReviews = extractAbnormalReviews(reviews, jiraHelper);
        String reviewsStatusTable = getReviewsStatusTable(upsourceId, reviews, jiraHelper);
        if (unVersionReviews.size() > 0) {
            reviewsStatusTable += getReviewsStatusTable(upsourceId, unVersionReviews, jiraHelper, "  Данные ревью не содержат фикс версии:");
        }
        if (abnormalReviews.size() > 0) {
            reviewsStatusTable += getReviewsStatusTable(upsourceId, abnormalReviews, jiraHelper, "  С данными ревью что то не так:");
        }
        return reviewsStatusTable;
    }

    private static String getReviewsStatusTable(String upsourceId, List<Review> reviews, JiraHelper jiraHelper) {
        return getReviewsStatusTable(upsourceId, reviews, jiraHelper, "");
    }

    private static String getReviewsStatusTable(String upsourceId, List<Review> reviews, JiraHelper jiraHelper, String title) {
        String message = title;
        String format = "%n%1$-13s|%2$11s|%3$-13s|%4$-3s|%5$5s|%6$3s";
        if (reviews.size() > 0) {
            message += "\n* " + upsourceId + " *";
            message += "\n```";
            message += "\n------------------------------------------------------";
            message += String.format(format, "Содевелопер", "Задача", "Ревьювер", "РИД", "Готов", "Стс");
            message += "\n------------------------------------------------------";
        }

        for (Review review : reviews) {
            String createdBy = getMappedReviewerName(review);
            String issueId = StringHelper.getIssueIdFromSvnRevisionComment(review.title());
            String completedRate = review.completionRate().completedCount + "/" + review.completionRate().reviewersCount;
            boolean status = !review.discussionCounter().hasUnresolved;
            String reviewId = StringHelper.getRegString(review.reviewId(), "\\w+-(\\d+)");
            String reviewer = getReviewerId(jiraHelper, issueId);
            message += String.format(format, createdBy, issueId, reviewer, reviewId, status, completedRate);
            review.title();
        }
        if (reviews.size() > 0) {
            message += "\n------------------------------------------------------";
            message += "\n```";
        }
        return message;
    }

    private static List<Review> extractUnVersionReviews(List<Review> reviews, JiraHelper jiraHelper) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews) {
            String issueId = StringHelper.getIssueIdFromSvnRevisionComment(review.title());
            Issue issue = jiraHelper.getIssue(issueId);
            Iterable<Version> fixVersions = issue.getFixVersions();

            boolean fixVersionsIsEmpty = fixVersions == null || !fixVersions.iterator().hasNext();
            if (issue.getAssignee() != null && fixVersionsIsEmpty) {
                result.add(review);
            }
        }
        for (Review abnormalReview : result) {
            reviews.remove(abnormalReview);
        }

        return result;
    }

    private static List<Review> extractAbnormalReviews(List<Review> reviews, JiraHelper jiraHelper) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews) {
            String issueId = StringHelper.getIssueIdFromSvnRevisionComment(review.title());
            Issue issue = jiraHelper.getIssue(issueId);
            Status status = issue.getStatus();
            String reviewId = StringHelper.getRegString(review.reviewId(), "\\w+-(\\d+)");

            boolean isInReview = status.getName().matches("Awaiting Review|In Review|Resolved");
            User assignee = issue.getAssignee();
            if ((assignee != null && !isInReview) || (assignee != null && assignee.getName().equals(reviewId))) {
                result.add(review);
            }
        }
        for (Review abnormalReview : result) {
            reviews.remove(abnormalReview);
        }

        return result;
    }

    private static String getMappedReviewerName(Review review) {
        return Common.UPSOURCE.userIdOnNameMap.get(review.createdBy());
    }

    private static String getReviewerId(JiraHelper jiraHelper, String issueId) {
        Issue issue = jiraHelper.getIssue(issueId);
        User assignee = issue.getAssignee();
        return assignee == null ? "unassigned" : assignee.getName();
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
        UpsourceApi upsourceApi = getUpsourceApi();
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
        if (message.text().contains(TITLE)) {
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

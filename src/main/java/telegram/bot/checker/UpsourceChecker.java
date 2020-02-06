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
import com.pengrad.telegrambot.request.PinChatMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import javafx.util.Pair;
import telegram.bot.data.Common;
import telegram.bot.data.LoginData;
import telegram.bot.data.chat.ChatData;
import telegram.bot.helper.BotHelper;
import telegram.bot.rules.ReLoginRule;
import upsource.ReviewState;
import upsource.UpsourceApi;
import upsource.dto.Review;
import upsource.dto.UpsourceUser;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static helper.logger.ConsoleLogger.logFor;

public class UpsourceChecker extends Thread {
    public static final String TITLE = "Господа, ваши содевелоперы, ожидают фидбэка по ревью, Будьте бдительны, Не заставляйте их ждать!!!";
    private TelegramBot bot;
    private List<Message> messagesToRemove;

    public UpsourceChecker(TelegramBot bot) {
        this.bot = bot;
        this.messagesToRemove = new ArrayList<>();
    }

    private static UpsourceApi getUpsourceApi() {
        return new UpsourceApi(Common.UPSOURCE.url, Common.UPSOURCE.login, Common.UPSOURCE.pass);
    }

    private static String getUpsourceViewResult(TelegramBot bot, UpsourceApi upsourceApi, String upsourceId, LoginData jiraLoginData) throws IOException {
        JiraHelper jiraHelper = JiraHelper.tryToGetClient(jiraLoginData, true, e -> ReLoginRule.tryToRelogin(bot, e, jiraLoginData));
        try {
            return getUpsourceViewResult(jiraHelper, upsourceApi, upsourceId);
        } finally {
            jiraHelper.disconnect();
        }
    }

    private static String getUpsourceViewResult(JiraHelper jiraHelper, UpsourceApi upsourceApi, String upsourceId) throws IOException {
        List<Review> upsourceReviews = upsourceApi.getProject(upsourceId)
            .getReviewsProvider(true)
//            .withDuration(TimeUnit.DAYS.toMillis(1))
            .withState(ReviewState.OPEN)
            .withCompleteCount(0, CountCondition.MORE_THAN_OR_EQUALS)
            .withReviewersCount(0, CountCondition.MORE_THAN)
            .getReviews().stream().sorted(Comparator.comparing((review) -> getMappedReviewerName(review).toLowerCase())).collect(Collectors.toList());
        List<JiraUpsourceReview> reviews = convertToJiraReviews(upsourceReviews).stream().sorted(Comparator.comparing((review) -> getMappedReviewerName(review.upsourceReview).toLowerCase() + review.issueId)).collect(Collectors.toList());

        List<JiraUpsourceReview> unVersionReviews = extractUnVersionReviews(reviews, jiraHelper);
        List<JiraUpsourceReview> abnormalReviews = extractAbnormalReviews(reviews, jiraHelper);
        String reviewsStatusTable = getReviewsStatusTable(upsourceId, reviews, jiraHelper);
        if (unVersionReviews.size() > 0) {
            reviewsStatusTable += getReviewsStatusTable(upsourceId, unVersionReviews, jiraHelper, "  Данные ревью не содержат фикс версии:");
        }
        if (abnormalReviews.size() > 0) {
            reviewsStatusTable += getReviewsStatusTable(upsourceId, abnormalReviews, jiraHelper, "🔥🔥🔥\n  С данными ревью что то не так:");
        }
        return reviewsStatusTable;
    }

    private static List<JiraUpsourceReview> convertToJiraReviews(List<Review> upsourceReviews) {
        List<JiraUpsourceReview> result = new ArrayList<>();
        for (Review upsourceReview : upsourceReviews) {
            for (String issueId : StringHelper.getIssueIdsFromSvnRevisionComment(upsourceReview.title())) {
                result.add(new JiraUpsourceReview(issueId, upsourceReview));
            }
        }
        return result;
    }

    private static String getReviewsStatusTable(String upsourceId, List<JiraUpsourceReview> reviews, JiraHelper jiraHelper) {
        return getReviewsStatusTable(upsourceId, reviews, jiraHelper, "");
    }

    private static String getReviewsStatusTable(String upsourceId, List<JiraUpsourceReview> reviews, JiraHelper jiraHelper, String title) {
        String message = (title.isEmpty() ? "" : "\n") + title;
        final String format = "%n%1$-13s|%2$11s|%3$-13s|%4$-3s|%5$5s|%6$3s";
        boolean hasReviews = reviews.size() > 0;
        if (hasReviews) {
            message += "\n* " + upsourceId + " *";
            message += "\n```";
            message += "\n------------------------------------------------------";
            message += String.format(format, "Содевелопер", "Задача", "Ревьювер", "РИД", "Готов", "Стс");
            message += "\n------------------------------------------------------";
        }
        Function<JiraUpsourceReview, String> reviewStringFunction = review -> {
            String createdBy = getMappedReviewerName(review.upsourceReview);
            String completedRate = review.upsourceReview.completionRate().completedCount + "/" + review.upsourceReview.completionRate().reviewersCount;
            boolean status = !review.upsourceReview.discussionCounter().hasUnresolved;
            String reviewId = StringHelper.getRegString(review.upsourceReview.reviewId(), "\\w+-(\\d+)");
            String reviewer = getReviewerId(jiraHelper, review.issueId);
            return String.format(format, createdBy, review.issueId, reviewer, reviewId, status, completedRate);
        };
        for (JiraUpsourceReview review : reviews) {
            message += reviewStringFunction.apply(review);
        }
        if (hasReviews) {
            message += "\n------------------------------------------------------";
            message += "\n```";
            message += "\n Задачи: ";
            List<String> issueLinks = extractIssueLinks(reviews, jiraHelper);
            for (String issueLink : issueLinks) {
                message += " " + issueLink;
            }
            message += "\n Ревьюхи: ";
            List<String> reviewLinks = extractReviewLinks(reviews, jiraHelper);
            for (String reviewLink : reviewLinks) {
                message += " " + reviewLink;
            }
            message += "\n ";
            List<String> users = extractReviewers(reviews, jiraHelper);
            for (String user : users) {
                message += " " + user;
            }
        }

        return message;
    }

    private static List<String> extractReviewLinks(List<JiraUpsourceReview> reviews, JiraHelper jiraHelper) {
        List<String> result = new ArrayList<>();
        for (JiraUpsourceReview review : reviews) {
            String linkedReviewKey = BotHelper.getLink(
                String.format(
                    "%s%s/review/%s",
                    Common.UPSOURCE.url,
                    review.upsourceReview.projectId(),
                    review.upsourceReview.reviewId()
                ),
                review.upsourceReview.reviewId()
            );
            if (!result.contains(linkedReviewKey)) {
                result.add(linkedReviewKey);
            }
        }
        return result;
    }

    private static List<String> extractIssueLinks(List<JiraUpsourceReview> reviews, JiraHelper jiraHelper) {
        List<String> result = new ArrayList<>();
        for (JiraUpsourceReview review : reviews) {
            Issue issue = jiraHelper.getIssue(review.issueId);
            String linkedIssueKey = JiraCheckerHelper.getTelegramIssueLink(issue);
            if (!result.contains(linkedIssueKey)) {
                result.add(linkedIssueKey);
            }
        }
        return result;
    }

    private static List<String> extractReviewers(List<JiraUpsourceReview> reviews, JiraHelper jiraHelper) {
        List<String> result = new ArrayList<>();
        for (JiraUpsourceReview review : reviews) {
            Issue issue = jiraHelper.getIssue(review.issueId);
            User assignee = issue.getAssignee();
            String telegramLinkOnUser = JiraCheckerHelper.getUserLoginWithTelegramLinkOnUser(assignee);
            if (!result.contains(telegramLinkOnUser)) {
                result.add(telegramLinkOnUser);
            }
        }
        return result;
    }

    private static List<JiraUpsourceReview> extractUnVersionReviews(List<JiraUpsourceReview> reviews, JiraHelper jiraHelper) {
        List<JiraUpsourceReview> result = new ArrayList<>();
        for (JiraUpsourceReview review : reviews) {
            String issueId = review.issueId;
            Issue issue = jiraHelper.getIssue(issueId);
            Iterable<Version> fixVersions = issue.getFixVersions();

            boolean fixVersionsIsEmpty = fixVersions == null || !fixVersions.iterator().hasNext();
            if (issue.getAssignee() != null && fixVersionsIsEmpty) {
                result.add(review);
            }
        }
        for (JiraUpsourceReview abnormalReview : result) {
            reviews.remove(abnormalReview);
        }

        return result;
    }

    private static List<JiraUpsourceReview> extractAbnormalReviews(List<JiraUpsourceReview> reviews, JiraHelper jiraHelper) {
        List<JiraUpsourceReview> result = new ArrayList<>();
        for (JiraUpsourceReview review : reviews) {
            String issueId = review.issueId;
            Issue issue = jiraHelper.getIssue(issueId);
            Status status = issue.getStatus();
            String summary = issue.getSummary();
            String createdBy = getMappedReviewerName(review.upsourceReview);
            boolean isInReview = summary.contains("IN REVIEW") || status.getName().matches("Awaiting Review|In Review|Resolved");
            User assignee = issue.getAssignee();
            if ((assignee != null && !isInReview) || (assignee != null && assignee.getName().equals(createdBy))) {
                result.add(review);
            }
        }
        for (JiraUpsourceReview abnormalReview : result) {
            reviews.remove(abnormalReview);
        }

        return result;
    }

    private static String getMappedReviewerName(Review review) {
        String createdBy = Common.UPSOURCE.userIdOnNameMap.get(review.createdBy());
        if (createdBy == null) {
            for (UpsourceUser participant : review.participants()) {
                if (participant.role == 1) {
                    createdBy = Common.UPSOURCE.userIdOnNameMap.get(participant.userId);
                    break;
                }
            }
        }
        return createdBy;
    }

    private static String getReviewerId(JiraHelper jiraHelper, String issueId) {
        Issue issue = jiraHelper.getIssue(issueId);
        User assignee = issue.getAssignee();
        return assignee == null ? "unassigned" : assignee.getName();
    }

    public static void updateMessage(TelegramBot bot, String upsourceProjectId, Message message, LoginData jiraLoginData) {
        UpsourceApi upsourceApi = getUpsourceApi();
        String upsourceViewResult;
        try {
            upsourceViewResult = getUpsourceViewResult(bot, upsourceApi, upsourceProjectId, jiraLoginData);
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(UpsourceChecker.class, e);
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
            ConsoleLogger.logErrorFor(UpsourceChecker.class, e);
        }
    }

    private static InlineKeyboardMarkup getReplyMarkup(String upsourceProjectId) {
        return new InlineKeyboardMarkup(new InlineKeyboardButton[] {
            new InlineKeyboardButton("Обновить")
                .callbackData("update_upsource_checker_view_result_for:" + upsourceProjectId),
            new InlineKeyboardButton("Подробнее")
                .callbackData("show_upsource_checker_tabs_description"),
            new InlineKeyboardButton("Что может быть не так?")
                .callbackData("show_upsource_checker_possible_problems")
        });
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
            try {
                check();
            } catch (IOException e) {
                ConsoleLogger.logErrorFor(this, e);
                return;
            }
        }
    }

    private boolean sleepToNextCheck() throws InterruptedException {
        long minutesUntilTargetHour = getMinutesUntilNextTargetHour();
        logFor(this, "sleepToNextCheck: " + minutesUntilTargetHour + " minutes");
        TimeUnit.MINUTES.sleep(minutesUntilTargetHour);
        if (TimeHelper.isWeekends()) {
            TimeUnit.MINUTES.sleep(1);
            return false;
        }
        return true;
    }

    private Long getMinutesUntilNextTargetHour() {
        Long minutesUntilTargetHourForFirstPartOfDay = TimeHelper.getMinutesUntilTargetHour(10);
        Long minutesUntilTargetHourForSecondPartOfDay = TimeHelper.getMinutesUntilTargetHour(18);
        return Math.min(minutesUntilTargetHourForFirstPartOfDay, minutesUntilTargetHourForSecondPartOfDay);
    }

    public void check() throws IOException {
        removeMessages();
        UpsourceApi upsourceApi = getUpsourceApi();
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            check(upsourceApi, chatData);
        }
    }

    private void removeMessages() {
        for (Message message : messagesToRemove) {
            BotHelper.removeMessage(bot, message);
        }
        messagesToRemove.clear();
    }

    public void check(ChatData chatData) throws IOException {
        UpsourceApi upsourceApi = getUpsourceApi();
        check(upsourceApi, chatData);
    }

    public void check(UpsourceApi upsourceApi, ChatData chatData) throws IOException {
        logFor(this, String.format("check:start: %s(%s)", chatData.getChatName(), chatData.getUpsourceIds().toString()));
        List<Pair<String, String>> messages = new ArrayList<>();
        for (String upsourceId : chatData.getUpsourceIds()) {
            String message = getUpsourceViewResult(bot, upsourceApi, upsourceId, chatData.getJiraConfig().getLoginData());
            if (message.length() > 0) {
                messages.add(new Pair<>(upsourceId, message));
            }
        }
        sendMessagesWithViewResult(chatData, messages);
        logFor(this, "check:end");
    }

    private void sendMessagesWithViewResult(ChatData chatData, List<Pair<String, String>> messages) {
        if (messages.size() == 1) {
            Pair<String, String> projectIdOnMessagePair = messages.get(0);
            sendMessageWithInlineBtns(chatData, TITLE + projectIdOnMessagePair.getValue(), projectIdOnMessagePair.getKey());
        } else if (messages.size() > 0) {
            SendResponse sendResponse = BotHelper.sendMessage(bot, chatData.getChatId(), TITLE, ParseMode.Markdown);
            messagesToRemove.add(sendResponse.message());
            for (Pair<String, String> message : messages) {
                sendMessageWithInlineBtns(chatData, message.getValue(), message.getKey());
            }
        }
    }

    private void sendMessageWithInlineBtns(ChatData chatData, String message, String upsourceProjectId) {
        long chatId = chatData.getChatId();
        SendMessage request = new SendMessage(chatId, message)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(getReplyMarkup(upsourceProjectId));
        SendResponse execute = bot.execute(request);
        messagesToRemove.add(execute.message());
        bot.execute(new PinChatMessage(chatId, execute.message().messageId()));
    }
}

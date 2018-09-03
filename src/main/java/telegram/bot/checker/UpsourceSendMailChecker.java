package telegram.bot.checker;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import helper.file.SharedObject;
import helper.mail.MailHelper;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;
import upsource.ReviewState;
import upsource.UpsourceApi;
import upsource.dto.Review;
import upsource.dto.UpsourceUser;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.log;
import static telegram.bot.data.Common.BIG_GENERAL_GROUPS;

public class UpsourceSendMailChecker extends Thread {

    private static final String SHARED_OBJECT_URL = "/tmp/" + UpsourceSendMailChecker.class.getSimpleName() + ".ser";
    private final long timeout;

    public UpsourceSendMailChecker(long timeout) {
        this.timeout = timeout;
    }

    public static void main(String[] args) throws IOException {
        new UpsourceSendMailChecker(TimeUnit.MINUTES.toMillis(1))
            .check(getUpsourceApi(), BIG_GENERAL_GROUPS.get(BIG_GENERAL_GROUPS.size() - 1));
    }

    private static String getReviewerId(JiraHelper jiraHelper, String issueId) {
        Issue issue = jiraHelper.getIssue(issueId);
        User assignee = issue.getAssignee();
        return assignee == null ? "unassigned" : assignee.getName();
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

    private static UpsourceApi getUpsourceApi() {
        return new UpsourceApi(Common.UPSOURCE.url, Common.UPSOURCE.login, Common.UPSOURCE.pass);
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isWorkDay()) {
                try {
                    check();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isWorkDay() {
        return TimeHelper.checkToDayIs(DayOfWeek.MONDAY) ||
            TimeHelper.checkToDayIs(DayOfWeek.TUESDAY) ||
            TimeHelper.checkToDayIs(DayOfWeek.WEDNESDAY) ||
            TimeHelper.checkToDayIs(DayOfWeek.THURSDAY) ||
            TimeHelper.checkToDayIs(DayOfWeek.FRIDAY);
    }

    private void check() throws IOException {
        UpsourceApi upsourceApi = getUpsourceApi();
        for (ChatData chatData : BIG_GENERAL_GROUPS) {
            check(upsourceApi, chatData);
        }
    }

    public void check(UpsourceApi upsourceApi, ChatData chatData) throws IOException {
        log("UpsourceReadyForReviewChecker::check:" + chatData.getUpsourceIds().toString());
        HashMap<String, String> jiraAssignOn = SharedObject.loadMap(SHARED_OBJECT_URL, new HashMap<>());
        JiraHelper jiraHelper = JiraHelper.getClient(Common.JIRA, true);
        Map<String, List<String>> userMessages = new HashMap<>();
        Map<String, List<String>> userAbortedMessages = new HashMap<>();
        for (String upsourceId : chatData.getUpsourceIds()) {
            List<Review> upsourceReviews = getReviews(upsourceApi, upsourceId);
            List<JiraUpsourceReview> reviews = convertToJiraReviews(upsourceReviews);
            for (JiraUpsourceReview review : reviews) {
                String createdBy = getMappedReviewerName(review.upsourceReview);
                Issue issue = jiraHelper.getIssue(review.issueId);
                String reviewerName = getReviewerId(jiraHelper, review.issueId);
                String to = Common.UPSOURCE.userLoginOnMailMap.getOrDefault(reviewerName, "");
                String issueKey = issue.getKey();
                if (jiraAssignOn.containsKey(issueKey) && jiraAssignOn.get(issueKey).equals(to)) {
                    continue;
                }
                if (reviewerName == null) {
                    continue;
                }
                String linkedIssueKey = String.format("<a href=\"%2$s/browse/%1$s\">%1$s</a>", issueKey, Common.JIRA.url);
                if (!to.isEmpty() && createdBy.equals(reviewerName)) {
                    String message = String.format("<b>[%s]</b> as '%s' Please, Pay Attention;<br>\n", linkedIssueKey, issue.getSummary());
                    if (!userAbortedMessages.containsKey(to)) {
                        userAbortedMessages.put(to, new ArrayList<>());
                    }
                    userAbortedMessages.get(to).add(message);
                } else if (!to.isEmpty() && !createdBy.equals(reviewerName)) {
                    String message = String.format("<b>[%s]</b> as '%s' ready for review;<br>\n", linkedIssueKey, issue.getSummary());
                    if (!userMessages.containsKey(to)) {
                        userMessages.put(to, new ArrayList<>());
                    }
                    userMessages.get(to).add(message);
                }
                jiraAssignOn.put(issueKey, to);
                SharedObject.save(SHARED_OBJECT_URL, jiraAssignOn);
            }
            String title = "[" + upsourceId + "] Possible review was assigned to you";
            sendMail(userMessages, title, title);
            String abortedTitle = "[" + upsourceId + "] Possible review was back to you";
            sendMail(userAbortedMessages, title, abortedTitle);
        }
        log("UpsourceReadyForReviewChecker::check:end");

    }

    private void sendMail(Map<String, List<String>> messages, String titleOfMessageBody, String title) {
        for (Map.Entry<String, List<String>> entry : messages.entrySet()) {
            String to = entry.getKey();
            StringBuilder messageContent = new StringBuilder(title + ":<br>\n");
            for (String message : entry.getValue()) {
                messageContent.append(message);
            }
            MailHelper.tryToSendMail(to, titleOfMessageBody, messageContent.toString());
        }
    }

    private List<Review> getReviews(UpsourceApi upsourceApi, String upsourceId) throws IOException {
        return upsourceApi.getProject(upsourceId)
            .getReviewsProvider(true)
            .withState(ReviewState.OPEN)
            .withCompleteCount(0, CountCondition.MORE_THAN_OR_EQUALS)
            .withReviewersCount(0, CountCondition.MORE_THAN)
            .getReviews();
    }

}

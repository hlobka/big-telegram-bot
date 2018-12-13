package telegram.bot.checker;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static helper.logger.ConsoleLogger.logErrorFor;
import static helper.logger.ConsoleLogger.logFor;
import static telegram.bot.data.Common.BIG_GENERAL_GROUPS;

public class UpsourceSendMailChecker extends Thread {

    private static final String JIRA_ASSIGN_ON = "jiraAssignOn";
    private final long timeout;
    private final Supplier<JiraHelper> jiraHelperProvider;
    private boolean isFirstTime = true;
    //TODO: move to config file
    private static final List<String> FIX_VERSION_REQUIRED_COMMENT_LIST = Arrays.asList(
            "fix version required",
            "let's have fix ver here",
            "Pls add fix ver ",
            "fix version needs to be added",
            "fix version needed",
            "reopen because of fix version empty",
            "fix version empty"
    );

    public static void main(String[] args) {
        String issueKey = "JIRA-100500";
        String userName = "loginname";
        String testComment = new UpsourceSendMailChecker(1000, null)
                .getFixVersionRequiredComment();
        JiraHelper client = JiraHelper.getClient(Common.JIRA);
        Issue issue = client.getIssue(issueKey);
        Iterable<Version> fixVersions = issue.getFixVersions();
        if (fixVersions != null && !fixVersions.iterator().hasNext()) {
            client.assignIssueOn(issueKey, userName);
            client.addIssueComment(issueKey, testComment);
        }
    }

    public UpsourceSendMailChecker(long timeout, Supplier<JiraHelper> jiraHelperProvider) {
        this.timeout = timeout;
        this.jiraHelperProvider = jiraHelperProvider;
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
            if (isWorkDay()) {
                try {
                    check();
                } catch (IOException e) {
                    ConsoleLogger.logErrorFor(this, e);
                }
            }
            try {
                long timeout = isFirstTime ? 1 : this.timeout;
                isFirstTime = false;
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                ConsoleLogger.logErrorFor(this, e);
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
        logFor(this, String.format("check:%s[%s]", chatData.getChatName(), chatData.getUpsourceIds().toString()));
        HashMap<String, String> jiraAssignOn = SharedObject.load(this, JIRA_ASSIGN_ON, new HashMap<>());
        JiraHelper jiraHelper = jiraHelperProvider.get();
        Map<String, List<String>> userMessages = new HashMap<>();
        Map<String, List<String>> userAbortedMessages = new HashMap<>();
        for (String upsourceId : chatData.getUpsourceIds()) {
            List<Review> upsourceReviews = getReviews(upsourceApi, upsourceId);
            List<JiraUpsourceReview> reviews = convertToJiraReviews(upsourceReviews);
            for (JiraUpsourceReview review : reviews) {
                String reviewCreator = getMappedReviewerName(review.upsourceReview);
                Issue issue = jiraHelper.getIssue(review.issueId);
                String reviewerName = getReviewerId(jiraHelper, review.issueId);
                String issueAssignOn = Common.UPSOURCE.userLoginOnMailMap.getOrDefault(reviewerName, "");
                String issueKey = issue.getKey();
                if(issueAssignOn.isEmpty()){
                    try {
                        jiraHelper.assignIssueOn(issue.getKey(), reviewCreator);
                        jiraHelper.resetCache();
                        issue = jiraHelper.getIssue(review.issueId);
                        issueAssignOn = reviewCreator;
                    } catch (RestClientException e){
                        logErrorFor(this, e);
                    }
                }
                if (jiraAssignOn.containsKey(issueKey) && jiraAssignOn.get(issueKey).equals(issueAssignOn)) {
                    continue;
                }
                if (reviewerName == null) {
                    continue;
                }
                if (!reviewCreator.equals(issueAssignOn)) {
                    Iterable<Version> fixVersions = issue.getFixVersions();
                    boolean isFixVersionRequired = fixVersions != null && fixVersions.iterator().hasNext();
                    if (isFixVersionRequired) {
                        try {
                            jiraHelper.assignIssueOn(issueKey, reviewCreator);
                            jiraHelper.addIssueComment(issueKey, getFixVersionRequiredComment());
                            issueAssignOn = reviewCreator;
                        } catch (RestClientException e){
                            logErrorFor(this, e);
                        }
                    }
                }
                String linkedIssueKey = String.format("<a href=\"%2$s/browse/%1$s\">%1$s</a>", issueKey, Common.JIRA.url);
                String linkedReviewKey = String.format("<a href=\"%2$s/%3$s/review/%1$s\">%1$s</a>", review.upsourceReview.reviewId(), Common.UPSOURCE.url, upsourceId);
                boolean isAssignOnPresent = !issueAssignOn.isEmpty();
                if (isAssignOnPresent) {
                    if (reviewCreator.equals(reviewerName)) {
                        String message = String.format("<b>[%s]</b> as '%s' Please, Pay Attention; Link on review: %s<br>\n", linkedIssueKey, issue.getSummary(), linkedReviewKey);
                        if (!userAbortedMessages.containsKey(issueAssignOn)) {
                            userAbortedMessages.put(issueAssignOn, new ArrayList<>());
                        }
                        userAbortedMessages.get(issueAssignOn).add(message);
                    } else {
                        String message = String.format("<b>[%s]</b> as '%s' ready for review; Link on review: %s<br>\n", linkedIssueKey, issue.getSummary(), linkedReviewKey);
                        if (!userMessages.containsKey(issueAssignOn)) {
                            userMessages.put(issueAssignOn, new ArrayList<>());
                        }
                        userMessages.get(issueAssignOn).add(message);
                    }
                }
                jiraAssignOn.put(issueKey, issueAssignOn);
                SharedObject.save(this, JIRA_ASSIGN_ON, jiraAssignOn);
            }
            String title = "[" + upsourceId + "] Possible review was assigned to you";
            sendMail(userMessages, title, title);
            String abortedTitle = "[" + upsourceId + "] Possible review was back to you";
            sendMail(userAbortedMessages, title, abortedTitle);
        }
        logFor(this, "check:end");

    }

    private String getFixVersionRequiredComment() {
        List<String> comments = FIX_VERSION_REQUIRED_COMMENT_LIST;
        Collections.shuffle(comments);
        return comments.get(0);
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

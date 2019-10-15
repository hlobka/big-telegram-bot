package telegram.bot.checker.workFlow.implementations;

import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import helper.string.StringHelper;
import telegram.bot.checker.JiraCheckerHelper;
import telegram.bot.checker.JiraUpsourceReview;
import telegram.bot.checker.workFlow.ChatChecker;
import telegram.bot.checker.workFlow.implementations.services.JiraHelperServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.ServiceProvider;
import telegram.bot.checker.workFlow.implementations.services.UpsourceServiceProvider;
import telegram.bot.data.chat.ChatData;
import upsource.ReviewState;
import upsource.UpsourceApi;
import upsource.dto.Review;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static helper.logger.ConsoleLogger.logFor;

public class UnTrackedJiraIssuesOnReviewChecker implements ChatChecker {
    private final Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap;
    private final UpsourceServiceProvider upsourceServiceProvider;

    public UnTrackedJiraIssuesOnReviewChecker(Map<String, ServiceProvider<JiraHelper>> jiraHelperServiceProviderMap, UpsourceServiceProvider upsourceServiceProvider) {
        this.jiraHelperServiceProviderMap = jiraHelperServiceProviderMap;
        this.upsourceServiceProvider = upsourceServiceProvider;
    }

    @Override
    public Boolean isAccessibleToCheck(ChatData chatData) {
        return !chatData.getUpsourceIds().isEmpty() && !chatData.getJiraProjectKeyIds().isEmpty() && chatData.getIsEstimationRequired();
    }

    @Override
    public List<String> check(ChatData chatData) {
        logFor(this, "check:start");
        List<String> result = new ArrayList<>();
        String jiraUrl = chatData.getJiraConfig().getLoginData().url;
        ServiceProvider<JiraHelper> jiraHelperServiceProvider = jiraHelperServiceProviderMap.get(jiraUrl);
        jiraHelperServiceProvider.provide(jiraHelper -> {
            upsourceServiceProvider.provide(upsourceApi -> {
                for (String upsourceId : chatData.getUpsourceIds()) {
                    try {
                        List<Review> upsourceReviews = getReviews(upsourceApi, upsourceId);
                        List<JiraUpsourceReview> reviews = convertToJiraReviews(upsourceReviews);
                        for (JiraUpsourceReview review : reviews) {
                            String issueKey =review.issueId;
                            Boolean jiraTimeTracked = isJiraTimeTracked(jiraHelper, issueKey);
                            if (!jiraTimeTracked) {
                                String message =
                                    "🔥🔥🔥\n Данная задача ушла на ревью без лога времени: ** \n" +
                                        JiraCheckerHelper.getIssueDescription(jiraHelper.getIssue(issueKey));
                                result.add(
                                    message
                                );
                            }
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
        logFor(this, "check:end");
        return result;
    }

    private Boolean isJiraTimeTracked(JiraHelper jiraHelper, String jiraId) {
        boolean result = false;
        Issue issue = jiraHelper.getIssue(jiraId);
        TimeTracking timeTracking = issue.getTimeTracking();
        if (timeTracking != null) {
            Integer timeSpentMinutes = timeTracking.getTimeSpentMinutes();
            if (timeSpentMinutes != null && timeSpentMinutes > 0) {
                for (Worklog worklog : issue.getWorklogs()) {
                    if(!"code review".equalsIgnoreCase(worklog.getComment())){
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    private List<Review> getReviews(UpsourceApi upsourceApi, String upsourceId) throws IOException {
        return upsourceApi.getProject(upsourceId)
            .getReviewsProvider(true)
            .withState(ReviewState.OPEN)
            .withCompleteCount(0, CountCondition.MORE_THAN_OR_EQUALS)
            .withReviewersCount(0, CountCondition.MORE_THAN)
            .getReviews();
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
}

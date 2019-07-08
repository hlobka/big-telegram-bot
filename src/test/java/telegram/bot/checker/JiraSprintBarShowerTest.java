package telegram.bot.checker;

import atlassian.jira.FavoriteJqlScriptHelper;
import atlassian.jira.JiraHelper;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.pengrad.telegrambot.TelegramBot;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class JiraSprintBarShowerTest {

    @Test
    public void testRun() {
        JiraHelper jiraHelper = Mockito.mock(JiraHelper.class);
        TelegramBot bot = Mockito.mock(TelegramBot.class);
        JiraSprintBarShower testTarget = new JiraSprintBarShower(jiraHelper, bot, 1000L);

        List<Issue> mockedIssues = Arrays.asList(
            Mockito.mock(Issue.class),
            Mockito.mock(Issue.class),
            Mockito.mock(Issue.class)
        );
        TimeTracking mockedTimeTracking = Mockito.mock(TimeTracking.class);
        Mockito.when(mockedTimeTracking.getOriginalEstimateMinutes()).thenReturn(60);

        Mockito.when(mockedIssues.get(0).getTimeTracking()).thenReturn(mockedTimeTracking);
        Mockito.when(mockedIssues.get(1).getTimeTracking()).thenReturn(mockedTimeTracking);
        Mockito.when(mockedIssues.get(2).getTimeTracking()).thenReturn(mockedTimeTracking);
        Mockito.when(jiraHelper.getIssues(FavoriteJqlScriptHelper.getSprintAllIssuesJql("TEST"))).thenReturn(mockedIssues);

        Long totalTime = testTarget.getActiveSprintTotalHours("TEST");

        Assertions.assertThat(totalTime).isEqualTo(180);
    }
}
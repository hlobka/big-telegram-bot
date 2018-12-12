package telegram.bot;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import com.pengrad.telegrambot.TelegramBot;
import telegram.bot.checker.JenkinsChecker;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestJenkinsMain {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        JenkinsServer jenkins = new JenkinsServer(new URI(Common.JENKINS_URL));
        TelegramBot bot = new TelegramBot(Common.data.token);
        new JenkinsChecker(bot, TimeUnit.MINUTES.toMillis(20), Common.JENKINS_URL).run();
        for (Map.Entry<String, Job> entry : jenkins.getJobs().entrySet()) {
            for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
                for (String jenkinsId : chatData.getJenkinsIds()) {
                    if (entry.getKey().contains(jenkinsId)) {
                        logJob(entry.getValue(), entry.getKey());
                    }
                }
            }
        }
    }

    private static void logJob(Job job, String key) throws IOException {
        BuildResult result = job.details().getLastBuild().details().getResult();
        String url = job.getUrl();
        if(result.equals(BuildResult.FAILURE)) {
            System.err.println("Entry: " + key + ": " + job.getName() + ", " + result);
            System.err.println("url: " + url);
        } else {
            System.out.println("Entry: " + key + ": " + job.getName() + ", " + result);
            System.out.println("url: " + url);
        }
    }
}

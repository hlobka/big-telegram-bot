package telegram.bot.checker;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import http.GetExecuter;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static helper.logger.ConsoleLogger.log;
import static telegram.bot.data.Common.JENKINS_STATUSES;

public class JenkinsChecker extends Thread {
    private TelegramBot bot;
    private long millis;
    private final JenkinsServer jenkins;
    private HashMap<String, Boolean> statuses;
    private Boolean isFirstTime = true;

    public JenkinsChecker(TelegramBot bot, long millis, String jenkinsServerUrl) throws URISyntaxException {
        this.bot = bot;
        this.millis = millis;
        jenkins = new JenkinsServer(new URI(jenkinsServerUrl));
        statuses = SharedObject.loadMap(JENKINS_STATUSES, new HashMap<String, Boolean>());
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                long millis = isFirstTime ? 1 : this.millis;
                isFirstTime = false;
                TimeUnit.MILLISECONDS.sleep(millis);
                check();
            } catch (InterruptedException e) {
                ConsoleLogger.logErrorFor(this, e);
                Thread.interrupted();
                return;
            } catch (IOException e) {
                ConsoleLogger.logErrorFor(this, e);
                return;
            }
        }
    }

    private void check() throws IOException {
        log("JenkinsChecker::check:start");
        Map<String, Job> jobs = jenkins.getJobs();
        Map<String, Job> internalJobs = new HashMap<>();
        List<String> jenkinsIds = new ArrayList<>();
        for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
            for (String jenkinsId : chatData.getJenkinsIds()) {
                if (!jenkinsIds.contains(jenkinsId)) {
                    jenkinsIds.add(jenkinsId);
                }
            }
        }
        for (String jenkinsId : jenkinsIds) {
            List<Job> jobList = jenkins.getViews(new FolderJob(jenkinsId, Common.JENKINS_JOBS_URL + jenkinsId)).get("all").getJobs();
            for (Job job : jobList) {
                String jobName = jenkinsId + "_" + job.getName();
                internalJobs.put(jobName, job);
            }
            for (Job job : jobs.values()) {
                if (job.getName().contains(jenkinsId)) {
                    internalJobs.put(job.getName(), job);
                }
            }
        }
        checkJobsStatus(internalJobs);
        log("JenkinsChecker::check:end");
    }

    private void checkJobsStatus(Map<String, Job> jobs) throws IOException {
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
                for (String jenkinsId : chatData.getJenkinsIds()) {
                    String key = entry.getKey();
                    Job job = entry.getValue();
                    if (key.contains(jenkinsId)) {
                        log("JenkinsChecker::checkJobsStatus:" + key + " for chat: " + chatData.getChatId());
                        checkJobStatus(chatData, key, job);
                    }
                }
            }
        }
    }

    private void checkJobStatus(ChatData chatData, String key, Job job) throws IOException {
        JobWithDetails jobWithDetails = job.details();
        BuildWithDetails lastBuildDetails = jobWithDetails.getLastBuild().details();
        BuildResult result = lastBuildDetails.getResult();
        long timestamp = lastBuildDetails.getTimestamp();
        String statusKey = getStatusKey(chatData, key, timestamp);
        if (statuses.containsKey(statusKey)) {
            return;
        }
        if (result == null) {
            return;
        }
        if (result.equals(BuildResult.NOT_BUILT) || result.equals(BuildResult.BUILDING)) {
            return;
        }

        List<Build> allBuilds = jobWithDetails.getAllBuilds();
        if (allBuilds == null) {
            allBuilds = jobWithDetails.getBuilds();
        }
        if (result.equals(BuildResult.SUCCESS)) {
            Boolean isBuildFixed = isBuildFixed(chatData, key, allBuilds);
            if (!isBuildFixed) {
                return;
            }
        }
        String msg = getBuildMessage(key, job, lastBuildDetails, allBuilds);
        log(msg);
        sendMessage(chatData, msg);
        statuses.put(statusKey, result.equals(BuildResult.SUCCESS));
        SharedObject.save(JENKINS_STATUSES, statuses);
    }

    private void sendMessage(ChatData chatData, String msg) {
        SendMessage request = new SendMessage(chatData.getChatId(), msg)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(false);
        bot.execute(request);
    }

    private String getBuildMessage(String key, Job job, BuildWithDetails lastBuildDetails, List<Build> allBuilds) throws IOException {
        BuildResult result = lastBuildDetails.getResult();
        String url = job.getUrl();
        url = getShortUrlAsLink(url, key);
        long successCount = getNumberOfSuccessBuilds(allBuilds);
        int totalBuilds = allBuilds.size();
        long failedCount = totalBuilds - successCount;
        String changes = getChanges(lastBuildDetails);
        String possibleException = getPossibleException(lastBuildDetails);

        return String.format("Entry: %s %nStatus: <b>%s</b> %nTotal builds: <b>%d</b>; %nSuccess builds:<b>%d</b>; %nFailed builds:<b>%d</b>%n%s%n%s", url, result, totalBuilds, successCount, failedCount, changes, possibleException);
    }

    private String getChanges(BuildWithDetails details) {
        StringBuilder result = new StringBuilder();
        BuildChangeSet changeSet = details.getChangeSet();
        if (changeSet != null) {
            result = new StringBuilder("Last Changes: \n");
            for (BuildChangeSetItem buildChangeSetItem : changeSet.getItems()) {
                result
                        .append("<b>").append(buildChangeSetItem.getAuthor().getFullName()).append("</b>")
                        .append(":").append(buildChangeSetItem.getMsg())
                        .append("\n");
            }
        }
        return result.toString();
    }

    private String getPossibleException(BuildWithDetails details) {
        String consoleOutputText = getConsoleText(details);
        if (StringHelper.hasRegString(consoleOutputText, ".*\\/(\\w+.hx:\\d+: \\w+ \\d+-\\d+ : .*)", 0)) {
            return "Errors: " + StringHelper.getRegString(consoleOutputText, ".*\\/(\\w+.hx:\\d+: \\w+ \\d+-\\d+ : .*)");
        }
        return "";
    }

    private String getConsoleText(BuildWithDetails details) {
        try {
            return details.getConsoleOutputText();
        } catch (IOException e) {
            return "";
        }
    }

    private Boolean isBuildFixed(ChatData chatData, String key, List<Build> allBuilds) throws IOException {
        Boolean isBuildFixed = false;
        for (Build build : allBuilds) {
            BuildWithDetails details = build.details();
            String previousBuildStatusKey = getStatusKey(chatData, key, details.getTimestamp());
            if (statuses.containsKey(previousBuildStatusKey) && details.getResult().equals(BuildResult.SUCCESS)) {
                isBuildFixed = false;
                break;
            }
            if (!details.getResult().equals(BuildResult.SUCCESS)) {
                isBuildFixed = true;
                break;
            }
        }
        return isBuildFixed;
    }

    private String getStatusKey(ChatData chatData, String key, long timestamp) {
        return key + timestamp + chatData.getChatId();
    }

    private String getShortUrlAsLink(String url, String urlName) {
        String shortUrl = url;
        try {
            shortUrl = String.format("<a href=\"%s\">%s</a>", getShortUrl(url), urlName);
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(this, e);
        }
        return shortUrl;
    }

    private String getShortUrl(String url) throws IOException {
        String requestToShortUrl = "https://clck.ru/--?json=on&url=" + URLEncoder.encode(url, "UTF-8");
        return GetExecuter.getAsJsonArray(requestToShortUrl).get(0).getAsString();
    }

    private long getNumberOfSuccessBuilds(List<Build> allBuilds) throws IOException {
        int result = 0;
        for (Build allBuild : allBuilds) {
            if (allBuild.details().getResult().equals(BuildResult.SUCCESS)) {
                result++;
            }
        }
        return result;
    }
}

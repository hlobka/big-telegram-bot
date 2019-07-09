package telegram.bot.checker;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import helper.time.TimeHelper;
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

import static helper.logger.ConsoleLogger.logFor;
import static telegram.bot.data.Common.JENKINS_STATUSES;

//TODO: try to analise this jobs: http://master-jenkins/job/environment-jobs/
public class JenkinsChecker extends Thread {
    private TelegramBot bot;
    private long timeoutInMillis;
    private final JenkinsServer jenkins;
    private int maxNumberOfAttempts;
    private int idleTimeoutMultiplier = 1;
    private HashMap<String, Boolean> statuses;
    private int numberOfAttempts = -1;

    public JenkinsChecker(TelegramBot bot, long timeoutInMillis, String jenkinsServerUrl) throws URISyntaxException {
        this(bot, timeoutInMillis, jenkinsServerUrl, 0);
    }

    public JenkinsChecker(TelegramBot bot, long timeoutInMillis, String jenkinsServerUrl, int maxNumberOfAttempts) throws URISyntaxException {
        this.bot = bot;
        this.timeoutInMillis = timeoutInMillis;
        this.jenkins = new JenkinsServer(new URI(jenkinsServerUrl));
        this.maxNumberOfAttempts = maxNumberOfAttempts;
        this.statuses = SharedObject.loadMap(JENKINS_STATUSES, new HashMap<String, Boolean>());
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                sleepToNextCheck();
                check();
                numberOfAttempts = 0;
            } catch (InterruptedException e) {
                ConsoleLogger.logErrorFor(this, e);
                Thread.interrupted();
                return;
            } catch (IOException e) {
                ConsoleLogger.logErrorFor(this, e);
                if (numberOfAttempts++ > maxNumberOfAttempts) {
                    return;
                }
                ConsoleLogger.logError(e, String.format("will be rerun in %d minutes. Attempt: %d", TimeUnit.MILLISECONDS.toMinutes(getMillisToNextRun()), numberOfAttempts));
            }
        }
    }

    private long getMillisToNextRun() {
        long millis = numberOfAttempts == -1 ? 1 : timeoutInMillis;
        if (TimeHelper.isWeekends() || TimeHelper.isNight()) {
            millis = millis * idleTimeoutMultiplier;
        }
        return millis;
    }

    private void sleepToNextCheck() throws InterruptedException {
        long millis = getMillisToNextRun();
        logFor(this, "sleepToNextCheck: " + TimeUnit.MILLISECONDS.toMinutes(millis) + " minutes");
        TimeUnit.MILLISECONDS.sleep(millis);
    }

    private void check() throws IOException {
        logFor(this, "check:start");
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
        logFor(this, "check:end");
    }

    private void checkJobsStatus(Map<String, Job> jobs) throws IOException {
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
                for (String jenkinsId : chatData.getJenkinsIds()) {
                    String key = entry.getKey();
                    Job job = entry.getValue();
                    if (key.contains(jenkinsId)) {
                        logFor(this, "checkJobsStatus:" + key + " for chat: " + chatData.getChatName() + "[" + chatData.getChatId() + "]");
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
        String possibleException = getPossibleException(lastBuildDetails);
        String msg = getBuildMessage(key, job, lastBuildDetails, allBuilds, possibleException);
        logFor(this, msg);
        sendMessage(chatData, msg, possibleException.isEmpty());
        statuses.put(statusKey, result.equals(BuildResult.SUCCESS));
        SharedObject.save(JENKINS_STATUSES, statuses);
        this.statuses = SharedObject.loadMap(JENKINS_STATUSES, new HashMap<>());
    }

    private void sendMessage(ChatData chatData, String msg, boolean disableNotification) {
        SendMessage request = new SendMessage(chatData.getChatId(), msg)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(disableNotification);
        bot.execute(request);
    }

    private String getBuildMessage(String key, Job job, BuildWithDetails lastBuildDetails, List<Build> allBuilds, String possibleException) throws IOException {
        BuildResult result = lastBuildDetails.getResult();
        String url = job.getUrl();
        url = getShortUrlAsLink(url, key);
        long successCount = getNumberOfSuccessBuilds(allBuilds);
        int totalBuilds = allBuilds.size();
        long failedCount = totalBuilds - successCount;
        String changes = getChanges(lastBuildDetails);

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

    public JenkinsChecker withIdleTimeoutMultiplier(int multiplier){
        idleTimeoutMultiplier = multiplier;
        return this;
    }

    public JenkinsChecker withMaxNumberOfAttempts(int maxNumberOfAttempts){
        this.maxNumberOfAttempts = maxNumberOfAttempts;
        return this;
    }
}

package telegram.bot.checker;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import telegram.bot.data.Common;
import telegram.bot.data.chat.ChatData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static telegram.bot.data.Common.JENKINS_STATUSES;

public class JenkinsChecker extends Thread {
    private TelegramBot bot;
    private long millis;
    protected final JenkinsServer jenkins;
    private HashMap<String, Boolean> statuses;

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
                TimeUnit.MILLISECONDS.sleep(millis);
                check();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void check() throws IOException {
        System.out.println("JenkinsChecker::check");
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
    }

    private void checkJobsStatus(Map<String, Job> jobs) throws IOException {
        for (Map.Entry<String, Job> entry : jobs.entrySet()) {
            for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
                for (String jenkinsId : chatData.getJenkinsIds()) {
                    String key = entry.getKey();
                    Job job = entry.getValue();
                    if (key.contains(jenkinsId)) {
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
        String statusKey = key + timestamp + chatData.getChatId();
        if (statuses.containsKey(statusKey)) {
            return;
        }
        if (result.equals(BuildResult.SUCCESS) || result.equals(BuildResult.NOT_BUILT) || result.equals(BuildResult.BUILDING)) {
            return;
        }
        String url = job.getUrl();
        List<Build> allBuilds = jobWithDetails.getAllBuilds();
        if(allBuilds == null){
            allBuilds = jobWithDetails.getBuilds();
        }
        long successCount = getNumberOfSuccessBuilds(allBuilds);
        int totalBuilds = allBuilds.size();
        long failedCount = totalBuilds - successCount;
        String msg = String.format("Entry: %s: status: %s%n%s%nTotal builds:%d; Success builds:%d; Failed builds:%d", key, result, url, totalBuilds, successCount, failedCount);
        System.out.println(msg);
        SendMessage request = new SendMessage(chatData.getChatId(), msg)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(true)
            .disableNotification(false);
        bot.execute(request);
        statuses.put(statusKey, true);
        SharedObject.save(JENKINS_STATUSES, statuses);
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

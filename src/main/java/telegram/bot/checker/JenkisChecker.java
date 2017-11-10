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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static telegram.bot.data.Common.JENKINS_STATUSES;

public class JenkisChecker extends Thread {
    private TelegramBot bot;
    private long millis;
    protected final JenkinsServer jenkins;
    private HashMap<String, Boolean> statuses;
    public JenkisChecker(TelegramBot bot, long millis, String jenkinsServerUrl) throws URISyntaxException {
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
        System.out.println("JenkisChecker::check");
        for (Map.Entry<String, Job> entry : jenkins.getJobs().entrySet()) {
            for (ChatData chatData : Common.BIG_GENERAL_GROUPS) {
                for (String jenkinsId : chatData.getJenkinsIds()) {
                    String key = entry.getKey();
                    if (key.contains(jenkinsId)) {
                        Job job = entry.getValue();
                        JobWithDetails jobWithDetails = job.details();

                        BuildWithDetails lastBuildDetails = jobWithDetails.getLastBuild().details();

                        BuildResult result = lastBuildDetails.getResult();
                        long timestamp = lastBuildDetails.getTimestamp();
                        String statusKey = key + timestamp + chatData.getChatId();
                        if(statuses.containsKey(statusKey)){
                            continue;
                        }
                        if (result.equals(BuildResult.SUCCESS)||result.equals(BuildResult.NOT_BUILT)||result.equals(BuildResult.BUILDING)) {
                            continue;
                        }
                        String url = job.getUrl();
                        List<Build> allBuilds = jobWithDetails.getAllBuilds();
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
                }
            }
        }
    }

    private long getNumberOfSuccessBuilds(List<Build> allBuilds) throws IOException {
        int result = 0;
        for (Build allBuild : allBuilds) {
            if(allBuild.details().getResult().equals(BuildResult.SUCCESS)){
                result++;
            }
        }

        return result;
    }
}

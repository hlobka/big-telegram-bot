package telegram.bot.data;

import external.ExternalJob;
import telegram.bot.data.chat.ChatData;
import telegram.bot.data.chat.ChatPropertiesReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Common {
    public static final String ETS_USERS = "/tmp/ets_users.ser";
    public static final String ETS_USERS_IN_VACATION = "/tmp/ets_users_in_vacation.ser";
    public static final String COMMON_INT_DATA = "/tmp/common.ser";
    public static final String ACTION_ITEMS2 = "/tmp/actionItems2.ser";
    public static final String JENKINS_STATUSES = "/tmp/jenkinsStatuses.ser";
    public static final String JIRA_CHECKER_STATUSES = "/tmp/jiraCheckerStatuses.ser";
    public static final String RESOLVED_ACTION_ITEMS = "/tmp/resolvedActionItems.ser";
    public static final String LIKED_POSTS = "/tmp/likedPosts.ser";
    private static final Properties PROPERTIES = System.getProperties();
    public static final List<ChatData> BIG_GENERAL_GROUPS = new ArrayList<>();
    public static final Common data = new Common();
    public static final LoginData JIRA = new LoginData(PROPERTIES, "atlassian.jira");
    public static final LoginData EMAIL = new LoginData(PROPERTIES, "email");
    public static final GoogleData GOOGLE = new GoogleData(PROPERTIES);
    public static final UpsourceData UPSOURCE = new UpsourceData(PROPERTIES);
    public static final String HELP_LINK;
    public static final String HELP_LINKS;
    public static final String BIG_HELP_LINKS;
    public static final String JENKINS_URL;
    public static final String JENKINS_JOBS_URL;
    public static final List<ExternalJob> EXTERNAL_JOBS;

    static {
        HELP_LINK = PROPERTIES.getProperty("telegram.commands.help.file");
        HELP_LINKS = PROPERTIES.getProperty("telegram.commands.help_links.file");
        BIG_HELP_LINKS = PROPERTIES.getProperty("telegram.commands.help_links.big.file");
        JENKINS_URL = PROPERTIES.getProperty("jenkins.url");
        JENKINS_JOBS_URL = PROPERTIES.getProperty("jenkins.jobs.url");
        List<String> jobPropertiesFiles = Arrays.asList(PROPERTIES.getProperty("external.job.property.files").split(","));
        EXTERNAL_JOBS = jobPropertiesFiles.stream().filter(s -> !s.isEmpty()).map(Common::buildExternalJob).collect(Collectors.toList());
    }

    public List<ChatData> getChatForReport() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsReport).collect(Collectors.toList());
    }

    public List<Long> getMainGeneralChatIds() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsMainGeneral).map(ChatData::getChatId).collect(Collectors.toList());
    }

    private static ExternalJob buildExternalJob(String filePath) {
        Properties properties = System.getProperties();
        loadPropertiesFile("/" + filePath, properties);

        List<String> commandLines = new ArrayList<>();
        commandLines.add(properties.getProperty("external.job.executable.commandLine"));
        int i = 0;
        while (properties.containsKey("external.job.executable.commandLine" + i)) {
            commandLines.add(properties.getProperty("external.job.executable.commandLine" + i));
            i++;
        }
        return new ExternalJob(
                properties.getProperty("external.job.executable.folder"),
                properties.getProperty("external.job.executable.file"),
                commandLines,
                properties.getProperty("external.job.result.report"),
                getPropertyAsList(properties, "external.job.result.telegram.chat.ids.group")
        );
    }

    public final String token;

    private Common() {
        String configFile = "/config.properties";
        loadPropertiesFile(configFile, PROPERTIES);
        token = PROPERTIES.getProperty("telegram.bot.token");
        collectChatDatas();
    }

    private void collectChatDatas() {
        List<String> listOfChatConfigId = getPropertyAsList("telegram.chat.list");
        for (String chatConfigId : listOfChatConfigId) {
            ChatData chatData = getChatData(chatConfigId);
            BIG_GENERAL_GROUPS.add(chatData);
        }
    }

    public ChatData getChatData(String chatConfigId) {
        Properties chatProperties = new Properties();
        loadPropertiesFile(PROPERTIES.getProperty("telegram.chat." + chatConfigId), chatProperties);
        ChatPropertiesReader chatPropertiesReader = new ChatPropertiesReader(chatProperties);
        return new ChatData(
                chatPropertiesReader.getChatId(),
                chatPropertiesReader.getChatName(),
                chatPropertiesReader.getJenkinsIds(),
                chatPropertiesReader.getUpsourceIds(),
                chatPropertiesReader.getJiraIds(),
                chatPropertiesReader.isMainGeneralChat(),
                chatPropertiesReader.isGeneralChat(),
                chatPropertiesReader.isReportChat(),
                chatPropertiesReader.isSpamChat()
        );
    }

    private static List<String> getPropertyAsList(String property) {
        return Common.getPropertyAsList(PROPERTIES, property);
    }

    private static List<String> getPropertyAsList(Properties properties, String property) {
        return Arrays.asList(properties.getProperty(property).split(","));
    }

    private static void loadPropertiesFile(String filePath, Properties properties) {
        try {
            properties.load(Common.class.getResourceAsStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not loaded: " + filePath, e);
        }
    }

    public boolean isGeneralChat(Long chatId) {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsGeneral).map(ChatData::getChatId).collect(Collectors.toList()).contains(chatId);
    }

    public boolean isSpamChat(Long chatId) {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsSpam).map(ChatData::getChatId).collect(Collectors.toList()).contains(chatId);
    }

    public List<ChatData> getChatsFotSpam() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsSpam).collect(Collectors.toList());
    }
}

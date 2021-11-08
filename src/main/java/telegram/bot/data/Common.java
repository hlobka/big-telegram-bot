package telegram.bot.data;

import telegram.bot.data.chat.ChatData;
import telegram.bot.data.chat.ChatPropertiesReader;
import telegram.bot.data.jira.FavoriteJqlRules;
import telegram.bot.data.jira.FavoriteJqlRulesPropertiesReader;
import telegram.bot.helper.EtsHelper;
import telegram.bot.helper.PropertiesReadHelper;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class Common {
    public static final String ETS_USERS = "/tmp/ets_users.ser";
    public static final String ETS_USERS_IN_VACATION = "/tmp/ets_users_in_vacation.ser";
    public static final String ETS_USERS_WITH_ISSUES = "/tmp/ets_users_with_issues.ser";
    public static final String ETS_USERS_WITH_APPROVED_ISSUES = "/tmp/ets_users_with_approved_issues.ser";
    public static final String COMMON_INT_DATA = "/tmp/common.ser";
    public static final String ACTION_ITEMS2 = "/tmp/actionItems2.ser";
    public static final String JENKINS_STATUSES = getPathForJenkinsStatuses();
    public static final String JIRA_CHECKER_STATUSES = "/tmp/jiraCheckerStatuses.ser";
    public static final String RESOLVED_ACTION_ITEMS = "/tmp/resolvedActionItems.ser";
    public static final String LIKED_POSTS = "/tmp/likedPosts.ser";
    private static final Properties PROPERTIES = System.getProperties();
    public static final List<ChatData> BIG_GENERAL_GROUPS = new ArrayList<>();
    public final Map<String, FavoriteJqlRules> favoriteJqlRulesMap = new HashMap<>();
    public static final Common data = new Common();
    public static final LoginData EMAIL = new LoginData(PROPERTIES, "email");
    public static final GoogleData GOOGLE = new GoogleData(PROPERTIES);
    public static final UpsourceData UPSOURCE = new UpsourceData(PROPERTIES);
    public static final EtsHelper ETS_HELPER = new EtsHelper(ETS_USERS, ETS_USERS_IN_VACATION, ETS_USERS_WITH_ISSUES, ETS_USERS_WITH_APPROVED_ISSUES);
    public static final String HELP_LINK;
    public static final String HELP_LINKS;
    public static final String BIG_HELP_LINKS;
    public static final String JENKINS_URL;
    public static final String JENKINS_ADDITIONAL_URL;
    public static final String JENKINS_PROXY_HOST;
    public static final String JENKINS_PROXY_PORT;
    public static final String JENKINS_JOBS_URL;
    public static final String JENKINS_ADDITIONAL_JOBS_URL;
    public static final Map<String, Integer> USER_LOGIN_ON_TELEGRAM_ID_MAP = new PropertiesReadHelper(PROPERTIES).getStringIntMap("jiraLoginOnTelegramIdMap");
    public static final DayOfWeek ETS_DAY;

    static {
        HELP_LINK = PROPERTIES.getProperty("telegram.commands.help.file");
        HELP_LINKS = PROPERTIES.getProperty("telegram.commands.help_links.file");
        BIG_HELP_LINKS = PROPERTIES.getProperty("telegram.commands.help_links.big.file");
        JENKINS_URL = PROPERTIES.getProperty("jenkins.url");
        JENKINS_ADDITIONAL_URL = PROPERTIES.getProperty("jenkins.additional.url");
        JENKINS_PROXY_HOST = PROPERTIES.getProperty("jenkins.proxy.host");
        JENKINS_PROXY_PORT = PROPERTIES.getProperty("jenkins.proxy.port");
        JENKINS_JOBS_URL = PROPERTIES.getProperty("jenkins.jobs.url");
        JENKINS_ADDITIONAL_JOBS_URL = PROPERTIES.getProperty("jenkins.additional.jobs.url");
        ETS_DAY = DayOfWeek.of(Integer.parseInt(PROPERTIES.getProperty("ets.check.day")));
    }

    public List<ChatData> getChatForReport() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsReport).collect(Collectors.toList());
    }

    public List<Long> getMainGeneralChatIds() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsMainGeneral).map(ChatData::getChatId).collect(Collectors.toList());
    }

    public final String token;
    public final List<Long> telegramUserIdsWithGeneralAccess;


    private Common() {
        String configFile = "/config.properties";
        loadPropertiesFile(configFile, PROPERTIES);
        token = PROPERTIES.getProperty("telegram.bot.token");
        telegramUserIdsWithGeneralAccess = getPropertyAsList("telegram.user.id.available.list")
            .stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
        collectFavoriteJqlRulesMap();
        collectChatDatas();
    }

    private void collectFavoriteJqlRulesMap() {
        List<String> listOfTheJiraConfigs = getPropertyAsList("atlassian.jira.config.files.list");
        for (String jiraConfigFileId : listOfTheJiraConfigs) {
            favoriteJqlRulesMap.put(jiraConfigFileId, getJiraJqlRules(jiraConfigFileId));
        }
    }

    private FavoriteJqlRules getJiraJqlRules(String jiraConfigFileId) {
        Properties propertiesFile = new Properties();
        loadPropertiesFile(PROPERTIES.getProperty("atlassian.jira.config.path." + jiraConfigFileId), propertiesFile);
        FavoriteJqlRulesPropertiesReader chatPropertiesReader = new FavoriteJqlRulesPropertiesReader(propertiesFile);
        return new FavoriteJqlRules(
            chatPropertiesReader.getAllIssuesJql(),
            chatPropertiesReader.getAllEstimatedOrTrackedIssues(),
            chatPropertiesReader.getAllClosedAndEstimatedOrTrackedIssues(),
            chatPropertiesReader.getSprintAllIssuesJql(),
            chatPropertiesReader.getSprintClosedIssuesJql(),
            chatPropertiesReader.getSprintActiveIssuesJql(),
            chatPropertiesReader.getSprintOpenIssuesJql(),
            chatPropertiesReader.getSprintUnEstimatedIssuesJql(),
            chatPropertiesReader.getSprintUnTrackedIssuesJql(),
            chatPropertiesReader.getSprintClosedAndUnTrackedIssuesJql(),
            chatPropertiesReader.getLoginData()
        );
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
        try {
            return new ChatData(
                chatPropertiesReader.getChatId(),
                chatPropertiesReader.getChatName(),
                chatPropertiesReader.getJenkinsIds(),
                chatPropertiesReader.getJenkinsIdsForAllStatuses(),
                chatPropertiesReader.getUpsourceIds(),
                chatPropertiesReader.getJiraIds(),
                chatPropertiesReader.isEstimationRequired(),
                chatPropertiesReader.isMainGeneralChat(),
                chatPropertiesReader.isGeneralChat(),
                chatPropertiesReader.isReportChat(),
                chatPropertiesReader.isSpamChat(),
                favoriteJqlRulesMap.get(chatPropertiesReader.getJiraType()),
                chatPropertiesReader.getFilter()
            );
        } catch (RuntimeException e){
            throw new RuntimeException("can't parse chat data for: " + chatConfigId, e);
        }
    }

    private static List<String> getPropertyAsList(String property) {
        return Arrays.asList(PROPERTIES.getProperty(property).split(",")).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
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
        return !this.isGeneralChat(chatId) || BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsSpam).map(ChatData::getChatId).collect(Collectors.toList()).contains(chatId);
    }

    public List<ChatData> getChatsFotSpam() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsSpam).collect(Collectors.toList());
    }

    public List<ChatData> getGeneralChats() {
        return BIG_GENERAL_GROUPS.stream().filter(ChatData::getIsGeneral).collect(Collectors.toList());
    }

    public boolean hasChatData(Long chatId) {
        return BIG_GENERAL_GROUPS.stream().filter(chatData -> chatData.getChatId() == chatId).collect(Collectors.toList()).size() > 0;
    }

    public ChatData getChatData(Long chatId) {
        return BIG_GENERAL_GROUPS.stream().filter(chatData -> chatData.getChatId() == chatId).collect(Collectors.toList()).get(0);
    }

    public static String getPathForJenkinsStatuses() {
        return getPathForJenkinsStatuses("");
    }

    public static String getPathForJenkinsStatuses(String uniqueId) {
        return String.format("/tmp/jenkinsStatuses%s.ser", uniqueId);
    }
}

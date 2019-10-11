package telegram.bot.checker.workFlow.implementations.services;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import telegram.bot.data.LoginData;
import telegram.bot.rules.ReLoginRule;

import java.util.function.Consumer;

public class JiraHelperServiceProvider implements ServiceProvider<JiraHelper> {
    private JiraHelper jiraHelper;
    private final TelegramBot bot;
    private LoginData jiraLoginData;

    public JiraHelperServiceProvider(TelegramBot bot, LoginData jiraLoginData) {
        this.bot = bot;
        this.jiraLoginData = jiraLoginData;
    }

    @Override
    public void provide(Consumer<JiraHelper> consumer) {
        try {
            if (jiraHelper == null) {
                renew(consumer);
            } else {
                consumer.accept(jiraHelper);
            }
        } catch (RuntimeException e) {
            jiraHelper = null;
            renew(consumer);
        }
    }

    @Override
    public void renew(Consumer<JiraHelper> consumer) {
        jiraHelper = JiraHelper.tryToGetClient(jiraLoginData, false, e -> ReLoginRule.tryToRelogin(bot, e, jiraLoginData));
        consumer.accept(jiraHelper);
    }
}

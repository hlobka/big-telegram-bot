package telegram.bot.checker.workFlow.implementations.services;

import atlassian.jira.JiraHelper;
import com.pengrad.telegrambot.TelegramBot;
import telegram.bot.data.Common;
import telegram.bot.rules.ReLoginRule;

import java.util.function.Consumer;

public class JiraHelperServiceProvider implements ServiceProvider<JiraHelper> {
    private JiraHelper jiraHelper;
    private final TelegramBot bot;

    public JiraHelperServiceProvider(TelegramBot bot) {
        this.bot = bot;
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
        jiraHelper = JiraHelper.tryToGetClient(Common.JIRA, false, e -> ReLoginRule.tryToRelogin(bot, e));
        consumer.accept(jiraHelper);
    }
}

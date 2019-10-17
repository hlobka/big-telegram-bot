package telegram.bot.metrics.jira;

import java.util.concurrent.TimeUnit;

public interface JiraMetricsCollector {
    JiraMetricsProvider collect(TimeUnit timeUnit);
}

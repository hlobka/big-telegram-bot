package telegram.bot.metrics.jira;


import java.util.concurrent.TimeUnit;

public interface JiraMetricsCollector {
    JiraMetricsProvider collect(TimeUnit timeUnit, ProgressListener progressListener);
    JiraMetricsProvider collect(TimeUnit timeUnit);

    interface ProgressListener {
        void update(int currentStep, int maxSteps);
    }
}

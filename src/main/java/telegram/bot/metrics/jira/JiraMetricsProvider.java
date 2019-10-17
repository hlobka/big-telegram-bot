package telegram.bot.metrics.jira;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class JiraMetricsProvider {

    private final TimeUnit timeUnit;
    private final double budgetAtCompletion;
    private final Float sprintProgressFactor;
    private final long earnedValue;
    private final long actualCost;


    public Double getPlannedValue() {
        return (budgetAtCompletion * sprintProgressFactor);
    }

    public double getScheduleVariance() {
        return getEarnedValue() - getPlannedValue();
    }

    public double getSchedulePerformanceIndex() {
        return getEarnedValue() / getPlannedValue();
    }

    public double getCostVariance() {
        return getEarnedValue() - getActualCost();
    }

    public double getCostPerformanceIndex() {
        return (double) getEarnedValue() / getActualCost();
    }

    public double getEstimateAtCompletion() {
        return getBudgetAtCompletion() / getCostPerformanceIndex();
    }

    public double getEstimateToComplete() {
        return getEstimateAtCompletion() - getActualCost();
    }

    public double getVarianceAtCompletion() {
        return getBudgetAtCompletion() - getEstimateAtCompletion();
    }
}

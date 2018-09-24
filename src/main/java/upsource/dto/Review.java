package upsource.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Review {

    protected String projectId;
    protected String reviewId;
    protected String title;
    protected List<UpsourceUser> participants;
    protected Integer state;
    protected Boolean isUnread;
    protected Integer priority;
    protected Boolean isRemoved;
    protected String createdBy;
    protected Long createdAt;
    protected Long updatedAt;
    protected CompletionRate completionRate;
    protected DiscussionCounter discussionCounter;

    public String projectId() {
        return projectId;
    }

    public String reviewId() {
        return reviewId;
    }

    public String title() {
        return title;
    }

    public List<UpsourceUser> participants() {
        return participants;
    }

    public Integer state() {
        return state;
    }

    public Boolean isUnread() {
        return isUnread;
    }

    public Integer priority() {
        return priority;
    }

    public Boolean isRemoved() {
        return isRemoved;
    }

    public String createdBy() {
        return createdBy;
    }

    public Long createdAt() {
        return createdAt;
    }

    public Long updatedAt() {
        return updatedAt;
    }

    public CompletionRate completionRate() {
        return completionRate;
    }

    public DiscussionCounter discussionCounter() {
        return discussionCounter;
    }

    public static Review create(LinkedHashMap review){
        LinkedHashMap reviewId = (LinkedHashMap) review.get("reviewId");
        Review result = new Review();
        result.projectId = (String) reviewId.get("projectId");
        result.reviewId = (String) reviewId.get("reviewId");
        result.title = (String) review.get("title");
        result.participants = getParticipants((List<LinkedHashMap>) review.getOrDefault("participants", new ArrayList<>()));
        result.state = (Integer) review.get("state");
        result.isUnread = (Boolean) review.get("isUnread");
        result.priority = (Integer) review.get("priority");
        result.isRemoved = (Boolean) review.get("isRemoved");
        result.createdBy = (String) review.get("createdBy");
        result.createdAt = (Long) review.get("createdAt");
        result.updatedAt = (Long) review.get("updatedAt");
        result.completionRate = getCompletionRate((LinkedHashMap) review.get("completionRate"));
        result.discussionCounter = getDiscussionCounter((LinkedHashMap) review.get("discussionCounter"));
        return result;
    }

    private static List<UpsourceUser> getParticipants(List<LinkedHashMap> participants) {

        List<UpsourceUser> result = new ArrayList<>();
        for (LinkedHashMap participant : participants) {
            UpsourceUser user = new UpsourceUser();
            user.userId = (String) participant.get("userId");
            user.role = (Integer) participant.get("role");
            user.state = (Integer) participant.get("state");
            result.add(user);
        }

        return result;
    }

    private static DiscussionCounter getDiscussionCounter(LinkedHashMap discussionCounter) {
        DiscussionCounter result = new DiscussionCounter();
        result.counter = (Integer) discussionCounter.get("counter");
        result.hasUnresolved = (Boolean) discussionCounter.get("hasUnresolved");
        return result;
    }

    private static CompletionRate getCompletionRate(LinkedHashMap completionRate) {
        CompletionRate result = new CompletionRate();
        result.completedCount = (Integer) completionRate.get("completedCount");
        result.reviewersCount = (Integer) completionRate.get("reviewersCount");
        return result;
    }
}

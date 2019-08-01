package telegram.bot.checker;

import upsource.dto.Review;

public class JiraUpsourceReview {
    public final String issueId;
    public final Review upsourceReview;

    public JiraUpsourceReview(String issueId, Review upsourceReview) {
        this.issueId = issueId;
        this.upsourceReview = upsourceReview;
    }
}

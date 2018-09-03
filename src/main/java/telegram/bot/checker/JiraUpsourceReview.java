package telegram.bot.checker;

import upsource.dto.Review;

class JiraUpsourceReview {
    final String issueId;
    final Review upsourceReview;

    JiraUpsourceReview(String issueId, Review upsourceReview) {
        this.issueId = issueId;
        this.upsourceReview = upsourceReview;
    }
}

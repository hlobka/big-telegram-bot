package telegram.bot;

import telegram.bot.data.Common;
import upsource.ReviewState;
import upsource.UpsourceApi;
import upsource.dto.Review;
import upsource.dto.Revision;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpsourceMain {

    public static void main(String[] args) throws IOException {
        String url = Common.UPSOURCE.url;
        String login = Common.UPSOURCE.login;
        String pass = Common.UPSOURCE.pass;
        List<Review> reviewList = new UpsourceApi(url, login, pass)
            .getProject("wildfury")
            .getReviewsProvider()
            .withDuration(TimeUnit.DAYS.toMillis(7))
            .withState(ReviewState.OPEN)
            .withCompleteCount(1, CountCondition.MORE_THAN_OR_EQUALS)
            .getReviews();

        List<Revision> revisionList = new UpsourceApi(url, login, pass)
            .getProject("wildfury")
            .getRevisionsProvider()
            .getRevisions();
    }
}

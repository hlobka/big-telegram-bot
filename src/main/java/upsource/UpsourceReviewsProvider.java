package upsource;

import upsource.dto.Review;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UpsourceReviewsProvider {
    private UpsourceProject upsourceProject;
    private List<Predicate<Review>> filters = new ArrayList<>();

    UpsourceReviewsProvider(UpsourceProject upsourceProject) {
        this.upsourceProject = upsourceProject;
    }

    public UpsourceReviewsProvider withDuration(long milliseconds) {
        CountCondition countCondition = CountCondition.LESS_THAN;
        Predicate<Review> reviewPredicate = countCondition.getChecker(Review.class, review -> new Date().getTime() - review.createdAt(), milliseconds);
        filters.add(reviewPredicate);
        return this;
    }

    public UpsourceReviewsProvider withState(ReviewState state) {
        CountCondition countCondition = CountCondition.EQUALS;
        Predicate<Review> reviewPredicate = countCondition.getChecker(Review.class, Review::state, state.ordinal()+1);
        filters.add(reviewPredicate);
        return this;
    }

    public UpsourceReviewsProvider withCompleteCount(Integer count, CountCondition equals) {
        Predicate<Review> reviewPredicate = equals.getChecker(Review.class, review-> review.completionRate().completedCount, count);
        filters.add(reviewPredicate);
        return this;
    }

    public List<Review> getReviews() throws IOException {
        String url = upsourceProject.url;
        byte[] credentials = String.format("%s:%s", upsourceProject.userName, upsourceProject.pass).getBytes();
        String credentialsBase64 = Base64.getEncoder().encodeToString(credentials);
        RpmExecutor rpmExecutor = new RpmExecutor(url, credentialsBase64);
        Map<Object, Object> params = new HashMap<>();
        params.put("projectId", "wildfury");
        params.put("limit", 100);
        params.put("duration", "week");
        Object responseObject = rpmExecutor.doRequestJson("getReviews", params);
        LinkedHashMap responseResult = (LinkedHashMap) ((LinkedHashMap) responseObject).get("result");
        List<LinkedHashMap> reviews = (List<LinkedHashMap>) responseResult.get("reviews");
        List<Review> result = collectResults(reviews);
        for (Predicate<Review> filter : filters) {
            result = result.stream()
                .filter(filter)
                .collect(Collectors.toList());
        }
        return result;
    }

    private List<Review> collectResults(List<LinkedHashMap> reviews) {
        List<Review> result = new ArrayList<>();
        for (LinkedHashMap review : reviews) {
            Review reviewDto = Review.create(review);
            result.add(reviewDto);
        }
        return result;
    }
}

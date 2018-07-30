package upsource;

import upsource.dto.Review;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UpsourceReviewsProvider {
    private final String projectId;
    private final RpmExecutor rpmExecutor;
    private final Boolean useCache;
    private final List<Predicate<Review>> filters = new ArrayList<>();
    private List<Review> cachedResult;

    UpsourceReviewsProvider(String projectId, RpmExecutor rpmExecutor, Boolean useCache) {
        this.projectId = projectId;
        this.rpmExecutor = rpmExecutor;
        this.useCache = useCache;
    }

    public UpsourceReviewsProvider clearFilters() {
        filters.clear();
        return this;
    }

    public UpsourceReviewsProvider clearCache() {
        cachedResult = null;
        return this;
    }

    public UpsourceReviewsProvider withDuration(long milliseconds) {
        return withDuration(milliseconds, CountCondition.MORE_THAN_OR_EQUALS);
    }

    public UpsourceReviewsProvider withDuration(long milliseconds, CountCondition countCondition) {
        Predicate<Review> reviewPredicate = countCondition.getChecker(Review.class, review -> new Date().getTime() - review.createdAt(), milliseconds);
        filters.add(reviewPredicate);
        return this;
    }

    public UpsourceReviewsProvider withState(ReviewState state) {
        CountCondition countCondition = CountCondition.EQUALS;
        Predicate<Review> reviewPredicate = countCondition.getChecker(Review.class, Review::state, state.ordinal() + 1);
        filters.add(reviewPredicate);
        return this;
    }

    public UpsourceReviewsProvider withCompleteCount(Integer count, CountCondition equals) {
        Predicate<Review> reviewPredicate = equals.getChecker(Review.class, review -> review.completionRate().completedCount, count);
        filters.add(reviewPredicate);
        return this;
    }

    public UpsourceReviewsProvider withReviewersCount(int count, CountCondition countCondition) {
        Predicate<Review> reviewPredicate = countCondition.getChecker(Review.class, review -> review.completionRate().reviewersCount, count);
        filters.add(reviewPredicate);
        return this;
    }

    public List<Review> getReviews() throws IOException {
        return getReviews(100);
    }

    public List<Review> getReviews(int limit) throws IOException {
        List<Review> result = getReviewsFromServer(limit);
        result = applyFilters(result);
        return result;
    }

    private List<Review> applyFilters(List<Review> result) {
        for (Predicate<Review> filter : filters) {
            result = result.stream()
                .filter(filter)
                .collect(Collectors.toList());
        }
        return result;
    }

    private List<Review> getReviewsFromServer(int limit) throws IOException {
        if (cachedResult != null) {
            return cachedResult;
        }
        Map<Object, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("limit", limit);
        Object responseObject = rpmExecutor.doRequestJson("getReviews", params);
        LinkedHashMap responseResult = (LinkedHashMap) ((LinkedHashMap) responseObject).get("result");
        List<LinkedHashMap> reviews = (List<LinkedHashMap>) responseResult.getOrDefault("reviews", Collections.emptyList());
        List<Review> result = collectResults(reviews);
        if (useCache) {
            cachedResult = result;
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

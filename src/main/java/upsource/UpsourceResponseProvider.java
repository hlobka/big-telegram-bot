package upsource;

import upsource.dto.Review;
import upsource.filter.CountCondition;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UpsourceResponseProvider {
    private final String projectId;
    private final RpmExecutor rpmExecutor;
    private final List<Predicate<Review>> filters = new ArrayList<>();
    private List<Review> cachedResult;

    UpsourceResponseProvider(String projectId, RpmExecutor rpmExecutor) {
        this.projectId = projectId;
        this.rpmExecutor = rpmExecutor;
    }

    public UpsourceResponseProvider clearFilters() {
        filters.clear();
        return this;
    }

    public UpsourceResponseProvider clearCache() {
        cachedResult = null;
        return this;
    }

    public LinkedHashMap getReviewSummaryDiscussions(String reviewId) throws IOException {
        Map<Object, Object> revisionSet = new HashMap<>();
        revisionSet.put("selectAll", true);
        Map<Object, Object> reviewIdDto = new HashMap<>();
        reviewIdDto.put("projectId", projectId);
        reviewIdDto.put("reviewId", reviewId);
        Map<Object, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("reviewId", reviewIdDto);
        params.put("revisionSet", revisionSet);
        /*HashMap<Object, Object> anchor = new HashMap<>();
        anchor.put("reviewId", "340028");
        params.put("anchor", anchor);*/
//        params.put("text", text);
        Object responseObject = rpmExecutor.doRequestJson("getReviewSummaryDiscussions", params);
        return (LinkedHashMap) ((LinkedHashMap) responseObject).get("result");
    }
    public LinkedHashMap createDiscussion(String reviewId, String text) throws IOException {
        Map<Object, Object> reviewIdDto = new HashMap<>();
        reviewIdDto.put("projectId", projectId);
        reviewIdDto.put("reviewId", reviewId);
        Map<Object, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("reviewId", reviewIdDto);
        HashMap<Object, Object> anchor = new HashMap<>();
        anchor.put("reviewId", "340028");
        params.put("anchor", anchor);
        params.put("text", text);
//        params.put("limit", limit);
        Object responseObject = rpmExecutor.doRequestJson("createDiscussion", params);
        LinkedHashMap responseResult = (LinkedHashMap) ((LinkedHashMap) responseObject).get("result");
        return responseResult;
    }

    private List<LinkedHashMap> getReviewsFromServer(int limit) throws IOException {
        Map<Object, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("limit", limit);
        Object responseObject = rpmExecutor.doRequestJson("getReviews", params);
        LinkedHashMap responseResult = (LinkedHashMap) ((LinkedHashMap) responseObject).get("result");
        List<LinkedHashMap> reviews = (List<LinkedHashMap>) responseResult.getOrDefault("reviews", Collections.emptyList());
        return reviews;
    }


}

package upsource;

import upsource.dto.Revision;

import java.io.IOException;
import java.util.*;

public class UpsourceRevisionsProvider {
    private final String projectId;
    private final RpmExecutor rpmExecutor;

    public UpsourceRevisionsProvider(String projectId, RpmExecutor rpmExecutor) {
        this.projectId = projectId;
        this.rpmExecutor = rpmExecutor;
    }

    public List<Revision> getRevisions() throws IOException {
        return getRevisions(100);
    }

    public List<Revision> getRevisions(Integer limit) throws IOException {
        Map<Object, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("limit", limit);
        Object responseObject = rpmExecutor.doRequestJson("getRevisionsList", params);
        LinkedHashMap responseResult = (LinkedHashMap) ((LinkedHashMap) responseObject).get("result");
        List<LinkedHashMap> revisions = (List<LinkedHashMap>) responseResult.get("revision");
        List<Revision> result = collectResults(revisions);
        return result;
    }

    private List<Revision> collectResults(List<LinkedHashMap> reviews) {
        List<Revision> result = new ArrayList<>();
        for (LinkedHashMap revisions : reviews) {
            Revision reviewDto = Revision.create(revisions);
            result.add(reviewDto);
        }
        return result;
    }
}

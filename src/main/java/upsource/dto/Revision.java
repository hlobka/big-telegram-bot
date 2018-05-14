package upsource.dto;

import java.util.LinkedHashMap;
import java.util.List;

public class Revision {

    protected String projectId;
    protected String revisionId;
    protected Long revisionDate;
    protected Long effectiveRevisionDate;
    protected String revisionCommitMessage;
    protected Integer state;
    protected String vcsRevisionId;
    protected String shortRevisionId;
    protected String authorId;
    protected Integer reachability;
    protected List<String> branchHeadLabel;

    public String getProjectId() {
        return projectId;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public Long getRevisionDate() {
        return revisionDate;
    }

    public Long getEffectiveRevisionDate() {
        return effectiveRevisionDate;
    }

    public String getRevisionCommitMessage() {
        return revisionCommitMessage;
    }

    public Integer getState() {
        return state;
    }

    public String getVcsRevisionId() {
        return vcsRevisionId;
    }

    public String getShortRevisionId() {
        return shortRevisionId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Integer getReachability() {
        return reachability;
    }

    public List<String> getBranchHeadLabel() {
        return branchHeadLabel;
    }

    public static Revision create(LinkedHashMap linkedHashMap) {
        Revision result = new Revision();
        result.projectId = (String) linkedHashMap.get("projectId");
        result.revisionId = (String) linkedHashMap.get("revisionId");
        result.revisionDate = (Long) linkedHashMap.get("revisionDate");
        result.effectiveRevisionDate = (Long) linkedHashMap.get("effectiveRevisionDate");
        result.revisionCommitMessage = (String) linkedHashMap.get("revisionCommitMessage");
        result.state = (Integer) linkedHashMap.get("state");
        result.vcsRevisionId = (String) linkedHashMap.get("vcsRevisionId");
        result.shortRevisionId = (String) linkedHashMap.get("shortRevisionId");
        result.authorId = (String) linkedHashMap.get("authorId");
        result.reachability = (Integer) linkedHashMap.get("reachability");
        result.branchHeadLabel = (List<String>) linkedHashMap.get("branchHeadLabel");
        return result;
    }
}

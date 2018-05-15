package upsource;

import java.util.Base64;

public class UpsourceProject {
    protected String url;
    protected final String userName;
    protected final String pass;
    protected final String projectId;

    public UpsourceProject(String url, String userName, String pass, String projectId) {
        this.url = url;
        this.userName = userName;
        this.pass = pass;
        this.projectId = projectId;
    }

    public UpsourceReviewsProvider getReviewsProvider() {
        return getReviewsProvider(false);
    }

    public UpsourceReviewsProvider getReviewsProvider(Boolean useCache) {
        return new UpsourceReviewsProvider(projectId, getRpmExecutor(), useCache);
    }

    public UpsourceRevisionsProvider getRevisionsProvider() {
        return new UpsourceRevisionsProvider(projectId, getRpmExecutor());
    }

    private RpmExecutor getRpmExecutor() {
        byte[] credentials = String.format("%s:%s", userName, pass).getBytes();
        String credentialsBase64 = Base64.getEncoder().encodeToString(credentials);
        return new RpmExecutorImpl(url, credentialsBase64);
    }
}

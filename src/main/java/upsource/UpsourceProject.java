package upsource;

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
        return new UpsourceReviewsProvider(this);
    }
}

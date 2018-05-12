package upsource;

public class UpsourceApi {
    private String url;
    private final String userName;
    private final String pass;

    public UpsourceApi(String url, String userName, String pass) {
        this.url = url;
        this.userName = userName;
        this.pass = pass;
    }

    public UpsourceProject getProject(String projectId) {
        return new UpsourceProject(url, userName, pass, projectId);
    }
}

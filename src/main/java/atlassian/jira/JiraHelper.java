package atlassian.jira;

import com.atlassian.httpclient.apache.httpcomponents.ApacheAsyncHttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.*;
import com.atlassian.util.concurrent.Promise;
import helper.logger.ConsoleLogger;
import org.apache.commons.lang3.reflect.FieldUtils;
import telegram.bot.data.LoginData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class JiraHelper {

    private final JiraRestClient client;
    private final Boolean useCache;
    private Map<String, Issue> cacheOfIssues;
    private Consumer<RestClientException> errorHandler;

    private JiraHelper(JiraRestClient client, Boolean useCache) {
        this.client = client;
        this.useCache = useCache;
        cacheOfIssues = new HashMap<>();
    }

    public static JiraHelper tryToGetClient(LoginData loginData, Boolean useCache, Consumer<RestClientException> errorHandler) {
        return tryToGetClient(loginData, useCache, errorHandler, new AsynchronousJiraRestClientFactory());
    }

    public static JiraHelper tryToGetClient(LoginData loginData, Boolean useCache, Consumer<RestClientException> errorHandler, JiraRestClientFactory factory) {
        while (true) {
            try {
                URI uri = new URI(loginData.url);
                JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, loginData.login, loginData.pass);
                JiraHelper clientHelper = getClient(client, useCache);
                clientHelper.useErrorHandler(errorHandler);
                return clientHelper;
            } catch (RestClientException e) {
                ConsoleLogger.logErrorFor(JiraHelper.class, e);
                errorHandler.accept(e);
            } catch (URISyntaxException e) {
                ConsoleLogger.logErrorFor(JiraHelper.class, e);
                throw new RuntimeException(e);
            }
        }
    }

    private void useErrorHandler(Consumer<RestClientException> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public static JiraHelper getClient(LoginData loginData) {
        return getClient(loginData, false);
    }

    public static JiraHelper getClient(LoginData loginData, Boolean useCache) {
        try {
            URI uri = new URI(loginData.url);
            JiraRestClient client = getJiraRestClient(uri, loginData.login, loginData.pass);
            return getClient(client, useCache);
        } catch (URISyntaxException ex) {
            ConsoleLogger.logErrorFor(JiraHelper.class, ex);
            throw new RuntimeException(ex);
        }
    }

    public static AsynchronousJiraRestClient getJiraRestClient(URI serverUri, String username, String password) {
        AuthenticationHandler authenticationHandler = (new BasicHttpAuthenticationHandler(username, password));
        DisposableHttpClient httpClient = (new AsynchronousHttpClientFactory()).createClient(serverUri, authenticationHandler);
        AtlassianHttpClientDecorator atlassianHttpClientDecorator = (AtlassianHttpClientDecorator) httpClient;
        try {
            ApacheAsyncHttpClient httpClient1 = (ApacheAsyncHttpClient) FieldUtils.readField(atlassianHttpClientDecorator, "httpClient", true);
            ((HttpClientOptions) FieldUtils.readField(httpClient1, "httpClientOptions", true)).setSocketTimeout(2, TimeUnit.MINUTES);
        } catch (IllegalAccessException e) {
            ConsoleLogger.logErrorFor(JiraHelper.class, e);
        }
        return new AsynchronousJiraRestClient(serverUri, httpClient);
    }

    public static JiraHelper getClient(JiraRestClient client, Boolean useCache) {
        return new JiraHelper(client, useCache);
    }

    public void resetCache() {
        cacheOfIssues = new HashMap<>();
    }

    public Boolean hasIssue(String issueKey) {
        if (useCache && cacheOfIssues.containsKey(issueKey)) {
            return true;
        }
        try {
            return getIssue(issueKey, false) != null;
        } catch (RestClientException e) {
            for (ErrorCollection errorCollection : e.getErrorCollections()) {
                if(errorCollection.getErrorMessages().contains("Issue Does Not Exist")){
                    return false;
                }
            }
            return getIssue(issueKey, true) != null;
        }
    }

    public Issue getIssue(String issueKey) {
        return getIssue(issueKey, true);
    }

    public Issue getIssue(String issueKey, boolean checkRelogin) {
        if (useCache && cacheOfIssues.containsKey(issueKey)) {
            return cacheOfIssues.get(issueKey);
        }
        Promise<Issue> promise = client.getIssueClient().getIssue(issueKey);
        Issue issue;
        try {
            issue = promise.claim();
        } catch (RestClientException e){
            if(errorHandler != null && checkRelogin){
                errorHandler.accept(e);
                promise = client.getIssueClient().getIssue(issueKey);
                issue = promise.claim();
            } else {
                throw e;
            }
        }
        if (useCache) {
            cacheOfIssues.put(issueKey, issue);
        }
        return issue;
    }
}

package upsource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Map;

public class RpmExecutorImpl implements RpmExecutor {
    private final String url;
    private final String credentialsBase64;

    public RpmExecutorImpl(String url, String credentialsBase64) {
        this.url = url;
        this.credentialsBase64 = credentialsBase64;
    }

    @Override
    public String doRequest(String method, String paramsJson) throws IOException {
        // Upsource URL: http://<upsource-host>/~rpc/<method>
        String url = this.url + "~rpc/" + method;
        // Perform a POST request to pass a payload in the body.
        // Alternatively can make a GET request with "?params=paramsJson" query.
        PostMethod post = new PostMethod(url);
        // Basic authorization header. If not provided, the request will be executed with guest permissions.
        post.addRequestHeader("Authorization", "Basic " + credentialsBase64);
        post.setRequestBody(paramsJson);

        // Execute and return the response body.
        HttpClient client = new HttpClient();
        client.executeMethod(post);
        return post.getResponseBodyAsString();
    }

    @Override
    public Object doRequestJson(String method, Map<Object, Object> params) throws IOException {
        String inputJson = new ObjectMapper().writeValueAsString(params);
        String response = doRequest(method, inputJson);
        return new ObjectMapper().readValue(response, Map.class);
    }
}

package upsource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Map;

public class RpmExecutor {
    private final String url;
    private final String credentialsBase64;

    public RpmExecutor(String url, String credentialsBase64) {
        this.url = url;
        this.credentialsBase64 = credentialsBase64;
    }

    /**
     * Performs a request to Upsource.
     *
     * @param method     RPC method name
     * @param paramsJson input JSON as a string
     * @return output JSON as a string
     * @throws IOException if I/O error occurs
     */
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

    /**
     * Same as {@link #doRequest(String, String)}, but accepts a map rather than string.
     * The map is encoded into JSON, and method result is also decoded from JSON.
     *
     * @param method RPC method name
     * @param params input JSON as an object
     * @return output JSON as an object
     * @throws IOException if I/O error occurs
     */
    public Object doRequestJson(String method, Map<Object, Object> params) throws IOException {
        String inputJson = new ObjectMapper().writeValueAsString(params);
        String response = doRequest(method, inputJson);
        return new ObjectMapper().readValue(response, Map.class);
    }
}

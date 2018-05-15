package upsource;

import java.io.IOException;
import java.util.Map;

public interface RpmExecutor {
    /**
     * Performs a request to Upsource.
     *
     * @param method     RPC method name
     * @param paramsJson input JSON as a string
     * @return output JSON as a string
     * @throws IOException if I/O error occurs
     */
    String doRequest(String method, String paramsJson) throws IOException;
    /**
     * Same as {@link #doRequest(String, String)}, but accepts a map rather than string.
     * The map is encoded into JSON, and method result is also decoded from JSON.
     *
     * @param method RPC method name
     * @param params input JSON as an object
     * @return output JSON as an object
     * @throws IOException if I/O error occurs
     */
    Object doRequestJson(String method, Map<Object, Object> params) throws IOException;
}

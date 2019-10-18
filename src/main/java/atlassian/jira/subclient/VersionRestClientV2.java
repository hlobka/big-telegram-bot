package atlassian.jira.subclient;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;

public class VersionRestClientV2 extends AbstractAsynchronousRestClient {

    public VersionRestClientV2(HttpClient client) {
        super(client);
    }

    public VersionDto getVersion(URI uri) {
        Promise<VersionDto> promise = this.getAndParse(uri, new VersionJsonParserV2());
        return promise.claim();
    }
}
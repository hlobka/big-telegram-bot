package joke;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JokesUrlProviderTest {

    @Test
    public void testUrl() {
        String expectedURL = "http://umorili.herokuapp.com/api/get?site=bash.im&name=bash&num=1";
        assertThat(JokesUrlProvider.getURL()).isEqualTo(expectedURL);
    }

    @Test
    public void testUrlWithLimitedAmount() {
        String expectedURL = "http://umorili.herokuapp.com/api/get?site=bash.im&name=bash&num=10";
        assertThat(JokesUrlProvider.getURL(10)).isEqualTo(expectedURL);
    }

    @Test
    public void testUrlWithSiteAndLimitedAmount() {
        String expectedURL = "http://umorili.herokuapp.com/api/get?site=test.com&name=testName&num=50";
        assertThat(JokesUrlProvider.getURL("test.com", "testName", 50)).isEqualTo(expectedURL);
    }
}
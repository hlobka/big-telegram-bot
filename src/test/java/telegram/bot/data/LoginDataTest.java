package telegram.bot.data;

import helper.string.StringHelper;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginDataTest {
    @Test
    public void testConstructorWithInvalidValues() {
        LoginData loginData = new LoginData(new Properties(), "testType");

        assertThat(loginData.url).isNull();
        assertThat(loginData.login).isNull();
        assertThat(loginData.pass).isNull();
    }

    @Test
    public void testConstructor() {
        Properties properties = new Properties();
        properties.setProperty("testType.url", "testUrl");
        properties.setProperty("testType.auth.login", "testLogin");
        properties.setProperty("testType.auth.pass", "testPass");

        LoginData loginData = new LoginData(properties, "testType");

        assertThat(loginData.url).isEqualTo("testUrl");
        assertThat(loginData.login).isEqualTo("testLogin");
        assertThat(loginData.pass).isEqualTo("testPass");
    }

    @Test
    public void testConstructorWithCryptedPass() {
        Properties properties = new Properties();
        properties.setProperty("testType.url", "testUrl");
        properties.setProperty("testType.auth.login", "testLogin");
        properties.setProperty("testType.auth.pass", "testPass");
        properties.setProperty("testType.auth.cryptedPass", StringHelper.getAsSimpleCrypted("testCryptedPass"));

        LoginData loginData = new LoginData(properties, "testType");

        assertThat(loginData.url).isEqualTo("testUrl");
        assertThat(loginData.login).isEqualTo("testLogin");
        assertThat(loginData.pass).isEqualTo("testCryptedPass");
    }
}
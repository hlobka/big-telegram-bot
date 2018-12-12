package helper.string;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringHelperTest {
    @Test
    public void testGetRegString() {
        assertThat(StringHelper.getRegString("Кто такой Шива?", "кто такой ([a-zA-Zа-яА-Я]+)\\??"))
            .isEqualTo("Шива");
    }

    @Test
    public void testGetRegString1() {
        assertThat(StringHelper.getRegString("Кто такой Шива?", "(кто такой) ([a-zA-Zа-яА-Я]+)\\??", 2))
            .isEqualTo("Шива");
    }

    @Test
    public void testAllGetRegString1() {
        assertThat(StringHelper.getAllRegString("Кто такой Шива?", "(([a-zA-Zа-яА-Я]+) )", 2))
            .hasSize(2).contains("Кто", "такой");
    }

    @Test
    public void testReplace() {
//        String input = ":20:9405601140";
//        input.replaceAll("(:20):(\\d+)(?!\\d)", "$1");
//        input.replaceAll("(:20):(\\d+)(?!\\d)", "$1");
//        Assertions.assertThat(StringHelper.replaceGroups("Кто такой Шива?", "(кто такой) ([a-zA-Zа-яА-Я]+)\\??", 2))
//            .isEqualTo("Шива");
    }

    @Test
    public void testCryptedStrings() {
        String value = "asd";
        String cryptedAsd = StringHelper.getAsSimpleCrypted(value);
        String deCryptedAsd = StringHelper.getAsSimpleDeCrypted(cryptedAsd);
        assertThat(cryptedAsd).isEqualTo(cryptedAsd);
        assertThat(deCryptedAsd).isEqualTo(value);
    }

    @Test
    public void testGetIssueIdFromSvnRevisionComment() {
        assertThat(StringHelper.getIssueIdFromSvnRevisionComment("WILDFU-120, WILDFU-38 | stopping picker wheel according to server respone"))
            .isEqualTo("WILDFU-120");
        assertThat(StringHelper.getIssueIdFromSvnRevisionComment("WILDFU-144| popups refactoring"))
            .isEqualTo("WILDFU-144");
        assertThat(StringHelper.getIssueIdFromSvnRevisionComment("WILDFU-104 | Picker : Added helper for wheel rotation"))
            .isEqualTo("WILDFU-104");
        assertThat(StringHelper.getIssueIdFromSvnRevisionComment("WILDFU-104 WILDFU-38 | Picker : Added helper for wheel rotation"))
            .isEqualTo("WILDFU-104");
    }

    @Test
    public void testGetIssueIdsFromSvnRevisionComment() {
        assertThat(StringHelper.getIssueIdsFromSvnRevisionComment("WILDFU-120, WILDFU-38 | stopping picker wheel according to server respone"))
            .hasSize(2).contains("WILDFU-120", "WILDFU-38");
    }
}
package helper.string;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class StringHelperTest {
    @Test
    public void testGetRegString() {
        Assertions.assertThat(StringHelper.getRegString("Кто такой Шива?", "кто такой ([a-zA-Zа-яА-Я]+)\\??"))
            .isEqualTo("Шива");
    }

    @Test
    public void testGetRegString1() {
        Assertions.assertThat(StringHelper.getRegString("Кто такой Шива?", "(кто такой) ([a-zA-Zа-яА-Я]+)\\??", 2))
            .isEqualTo("Шива");
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
        Assertions.assertThat(cryptedAsd).isEqualTo(cryptedAsd);
        Assertions.assertThat(deCryptedAsd).isEqualTo(value);
    }

}
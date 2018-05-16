package telegram.bot.helper;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import telegram.bot.data.TelegramCriteria;

import static org.testng.Assert.*;

public class BotHelperTest {

    @Test
    public void testGetCuttedMessage() throws Exception {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < TelegramCriteria.MAX_MESSAGE_LENGTH; i++) {
            message.append("_");
        }
        String cuttedMessage = BotHelper.getCuttedMessage(message.toString());
        Assertions.assertThat(cuttedMessage).hasSize(TelegramCriteria.MAX_MESSAGE_LENGTH);
    }
}
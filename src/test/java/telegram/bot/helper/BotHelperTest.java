package telegram.bot.helper;

import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import telegram.bot.data.TelegramCriteria;

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

    @Test
    public void testGetDefaultLinkOnUser() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.id()).thenReturn(100500L);
        Mockito.when(user.firstName()).thenReturn("firstName");
        Mockito.when(user.lastName()).thenReturn("lastName");
        String linkOnUser = BotHelper.getLinkOnUser(user);

        Assertions.assertThat(linkOnUser).isEqualTo("[firstName lastName](tg://user?id=100500)");
    }

    @Test
    public void testGetMarkDownLinkOnUser() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.id()).thenReturn(100500L);
        Mockito.when(user.firstName()).thenReturn("firstName");
        Mockito.when(user.lastName()).thenReturn("lastName");
        String linkOnUser = BotHelper.getLinkOnUser(user, ParseMode.Markdown);

        Assertions.assertThat(linkOnUser).isEqualTo("[firstName lastName](tg://user?id=100500)");
    }

    @Test
    public void testGetHtmlLinkOnUser() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.id()).thenReturn(100500L);
        Mockito.when(user.firstName()).thenReturn("firstName");
        Mockito.when(user.lastName()).thenReturn("lastName");
        String linkOnUser = BotHelper.getLinkOnUser(user, ParseMode.HTML);

        Assertions.assertThat(linkOnUser).isEqualTo("<a href=\"tg://user?id=100500\">firstName lastName</a>");
    }

    @Test
    public void testGetMarkDownLinkOnUserWithCustomName() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.id()).thenReturn(100500L);
        Mockito.when(user.firstName()).thenReturn("firstName");
        Mockito.when(user.lastName()).thenReturn("lastName");
        String linkOnUser = BotHelper.getLinkOnUser(user, "TestName", ParseMode.Markdown);

        Assertions.assertThat(linkOnUser).isEqualTo("[TestName](tg://user?id=100500)");
    }

    @Test
    public void testGetHtmlLinkOnUserWithCustomName() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.id()).thenReturn(100500L);
        Mockito.when(user.firstName()).thenReturn("firstName");
        Mockito.when(user.lastName()).thenReturn("lastName");
        String linkOnUser = BotHelper.getLinkOnUser(user, "TestName", ParseMode.HTML);

        Assertions.assertThat(linkOnUser).isEqualTo("<a href=\"tg://user?id=100500\">TestName</a>");
    }
}
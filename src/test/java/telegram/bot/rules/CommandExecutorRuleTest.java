package telegram.bot.rules;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import telegram.bot.commands.Command;

import static org.testng.Assert.*;

public class CommandExecutorRuleTest {

    @Test
    public void testAddCallBackCommand() {
        CommandExecutorRule commandExecutorRule = new CommandExecutorRule(null);

        Assertions.assertThat(commandExecutorRule.callBackCommands)
            .isEmpty();
        commandExecutorRule.addCallBackCommand("test_command", null);

        Assertions.assertThat(commandExecutorRule.callBackCommands)
            .isNotEmpty()
        .containsKeys("test_command");
    }

    @Test
    public void testInvokeCallBackCommand() {
        CommandExecutorRule commandExecutorRule = new CommandExecutorRule(null);

        Command mockOfCommand = Mockito.mock(Command.class);
        Update mockOfUpdate = Mockito.mock(Update.class);
        CallbackQuery mockOfCallbackQuery = Mockito.mock(CallbackQuery.class);
        Mockito.when(mockOfUpdate.callbackQuery()).thenReturn(mockOfCallbackQuery);
        Mockito.when(mockOfCallbackQuery.id()).thenReturn("asd");
        Mockito.when(mockOfCallbackQuery.data()).thenReturn("test_command");
        commandExecutorRule.addCallBackCommand("test_command", mockOfCommand);

        commandExecutorRule.callback(mockOfUpdate);

        Mockito.verify(mockOfCommand).run(mockOfUpdate, "");
    }

    @Test
    public void testInvokeCallBackCommandWithArguments() {
        CommandExecutorRule commandExecutorRule = new CommandExecutorRule(null);

        Command mockOfCommand = Mockito.mock(Command.class);
        Update mockOfUpdate = Mockito.mock(Update.class);
        CallbackQuery mockOfCallbackQuery = Mockito.mock(CallbackQuery.class);
        Mockito.when(mockOfUpdate.callbackQuery()).thenReturn(mockOfCallbackQuery);
        Mockito.when(mockOfCallbackQuery.id()).thenReturn("asd");
        Mockito.when(mockOfCallbackQuery.data()).thenReturn("test_command:test_argument");
        commandExecutorRule.addCallBackCommand("test_command", mockOfCommand);

        commandExecutorRule.callback(mockOfUpdate);

        Mockito.verify(mockOfCommand).run(mockOfUpdate, "test_argument");
    }
}
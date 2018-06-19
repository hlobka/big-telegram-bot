package telegram.bot.helper;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import telegram.bot.dto.ActionItemDto;

import static org.testng.Assert.*;

public class ActionItemsHelperTest {

    @Test
    public void testSaveActionItem() {
        int key = ActionItemsHelper.resolved.saveActionItem("test", 1);
        ActionItemDto actionItemDto = ActionItemsHelper.resolved.readActionItem(key);
        Assertions.assertThat(key).isGreaterThan(0);
        Assertions.assertThat(actionItemDto).isNotNull();
        Assertions.assertThat(actionItemDto.getValue()).isEqualTo("test");
        Assertions.assertThat(actionItemDto.getChatId()).isEqualTo(1);
        Assertions.assertThat(actionItemDto.getDate()).isNotNull();

        ActionItemsHelper.resolved.removeActionItem(key);
        actionItemDto = ActionItemsHelper.resolved.readActionItem(key);
        Assertions.assertThat(actionItemDto).isNull();
    }
}
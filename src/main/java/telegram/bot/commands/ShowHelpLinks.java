package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import telegram.bot.data.Common;

public class ShowHelpLinks extends ShowInformationFromResource {
    public ShowHelpLinks() {
        super(ShowHelpLinks::getHelpLink, ParseMode.HTML);
    }

    private static String getHelpLink(Update update) {
        return Common.data.isGeneralChat(update.message().chat().id()) ? Common.BIG_HELP_LINKS : Common.HELP_LINKS;
    }
}

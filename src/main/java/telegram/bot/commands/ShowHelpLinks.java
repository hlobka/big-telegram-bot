package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import telegram.bot.data.Common;

import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;

public class ShowHelpLinks extends ShowInformationFromResource {
    public ShowHelpLinks() {
        super(ShowHelpLinks::getHelpLink, ParseMode.HTML);
    }

    private static String getHelpLink(Update update) {
        return BIG_GENERAL_GROUP_IDS.contains(update.message().chat().id()) ? Common.BIG_HELP_LINKS : Common.HELP_LINKS;
    }
}

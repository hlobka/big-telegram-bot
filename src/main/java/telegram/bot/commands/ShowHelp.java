package telegram.bot.commands;

import com.pengrad.telegrambot.model.request.ParseMode;

import static telegram.bot.data.Common.HELP_LINK;

public class ShowHelp extends ShowInformationFromResource {

    public ShowHelp() {
        super(HELP_LINK, ParseMode.HTML);
    }
}

package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.string.StringHelper;
import javafx.util.Pair;
import telegram.bot.data.Common;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;

public class ShowHelpLinks implements Command {
    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        try {
            String helpLink = getHelpLink(update);
            return new Pair<>(ParseMode.HTML, Collections.singletonList(StringHelper.getFileAsString(helpLink)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Big bot is a telegram bot to help organize work in team"));
    }

    private String getHelpLink(Update update) {
        return BIG_GENERAL_GROUP_IDS.contains(update.message().chat().id()) ? Common.BIG_HELP_LINKS : Common.HELP_LINKS;
    }
}

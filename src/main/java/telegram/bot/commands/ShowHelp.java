package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.string.StringHelper;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static telegram.bot.data.Common.HELP_LINK;

public class ShowHelp implements Command {

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        try {
            return new Pair<>(ParseMode.HTML, Collections.singletonList(StringHelper.getFileAsString(HELP_LINK)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Big bot is a telegram bot to help organize work in team"));
    }
}

package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ShowInformationFromResource implements Command {
    private Function<Update, String> filePathSupplier;
    private ParseMode parseMode;
    public ShowInformationFromResource(String filePathSupplier, ParseMode parseMode) {
        this(update -> filePathSupplier, parseMode);
    }

    public ShowInformationFromResource(Function<Update, String> filePathSupplier, ParseMode parseMode) {
        this.filePathSupplier = filePathSupplier;
        this.parseMode = parseMode;
    }

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        try {
            return new Pair<>(parseMode, Collections.singletonList(StringHelper.getFileAsString(filePathSupplier.apply(update))));
        } catch (IOException e) {
            ConsoleLogger.logErrorFor(this, e);
        }
        return new Pair<>(ParseMode.HTML, Collections.singletonList("Big bot is a telegram bot to help organize work in team"));
    }
}

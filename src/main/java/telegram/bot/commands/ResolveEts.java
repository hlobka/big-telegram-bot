package telegram.bot.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static telegram.bot.data.Common.ETS_USERS;

public class ResolveEts implements Command {
    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        ArrayList<String> strings = new ArrayList<>();
        User user = message.from();
        strings.add("Ets resolved for: " + user.firstName());
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        users.put(user, true);
        return new Pair<>(ParseMode.HTML, strings);
    }
}

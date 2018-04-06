package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import helper.file.SharedObject;
import javafx.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static telegram.bot.data.Common.ETS_USERS;

public class GetUserIdByNameCommand implements Command {

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            User user = entry.getKey();
            String userName = user.firstName() + user.lastName() + user.username();
            if(userName.toLowerCase().contains(values.toLowerCase())){
                return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("user id is: %d", user.id())));
            }
        }

        return new Pair<>(ParseMode.Markdown, Collections.singletonList(String.format("Unknown user with name: %s", values)));
    }
}

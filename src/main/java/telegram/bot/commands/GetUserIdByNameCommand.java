package telegram.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import javafx.util.Pair;
import telegram.bot.data.Common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetUserIdByNameCommand implements Command {

    @Override
    public Pair<ParseMode, List<String>> run(Update update, String values) {
        HashMap<User, Boolean> users = Common.ETS_HELPER.getUsers();
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

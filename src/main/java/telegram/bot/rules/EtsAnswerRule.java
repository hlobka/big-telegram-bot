package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import helper.file.SharedObject;
import telegram.bot.checker.EtsClarityChecker;

import java.util.HashMap;

import static telegram.bot.data.Common.ETS_USERS;

public class EtsAnswerRule implements Rule {
    private TelegramBot bot;

    public EtsAnswerRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(Update update) {

    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        if(callbackQuery.from()!= null && callbackQuery.data() != null && callbackQuery.data().equals("ets_resolved")){
            HashMap<User, Boolean> users = SharedObject.loadMap(ETS_USERS, new HashMap<User, Boolean>());
            users.put(callbackQuery.from(), true);
            SharedObject.save(ETS_USERS, users);
            if(EtsClarityChecker.LAST_MESSAGE_ID != -1){
                EtsClarityChecker.updateMessage(bot);
            }
        }
    }
}

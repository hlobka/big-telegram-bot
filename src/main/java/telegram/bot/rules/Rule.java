package telegram.bot.rules;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;

public interface Rule {
    void run(Update update);

    default boolean guard(Update update){
        return true;
    }

    default void callback(Update update) {
        callback(update.callbackQuery());
    }

    default void callback(CallbackQuery callbackQuery) {
        //nothing
    }
}

package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import telegram.bot.commands.ResolveEts;

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
        if (callbackQuery.from() != null && callbackQuery.data() != null && callbackQuery.data().equals("ets_resolved")) {
            ResolveEts.resolveUser(callbackQuery.from(), bot);
        }
    }
}

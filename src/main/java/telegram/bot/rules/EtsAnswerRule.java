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
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;
        if (isDataPresent){
            String data = callbackQuery.data();
            Long chatId = callbackQuery.message().chat().id();
            if(data.equals("ets_resolved")){
                ResolveEts.resolveUser(callbackQuery.from(), bot, chatId);
            } else if(data.equals("ets_on_vacation")){
                ResolveEts.sendUserOnVocation(callbackQuery.from(), bot, chatId);
            } else if(data.equals("ets_with_issue")){
                ResolveEts.setUserAsWithIssue(callbackQuery.from(), bot, chatId);
            }
        }
    }
}

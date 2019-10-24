package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import telegram.bot.commands.ResolveEts;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;

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
        User from = callbackQuery.from();
        if (isDataPresent){
            String data = callbackQuery.data();
            Long chatId = callbackQuery.message().chat().id();
            if(data.equals("ets_resolved")){
                ResolveEts.resolveUser(from, bot, chatId);
            } else if(data.equals("ets_on_vacation")){
                ResolveEts.sendUserOnVocation(from, bot, chatId);
            } else if(data.equals("ets_with_issue")){
                if(!Common.ETS_HELPER.isUserHasIssue(from)){
                    ResolveEts.setUserAsWithIssue(from, bot, chatId);
                } else {
                    BotHelper.alert(bot, callbackQuery.id(), "Sorry, You already have issue");
                }
            } else if(data.equals("ets_approve_users_with_issues")){
                if(Common.data.telegramUserIdsWithGeneralAccess.contains(from.id())) {
                    ResolveEts.approveAllUsersWithIssue(bot, chatId);
                } else {
                    BotHelper.alert(bot, callbackQuery.id(), "Sorry, this action only for PM");
                }
            }
        }
    }
}

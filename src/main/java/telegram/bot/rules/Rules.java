package telegram.bot.rules;

import com.pengrad.telegrambot.model.Update;

import java.util.ArrayList;
import java.util.List;

public class Rules {
    private final List<Rule> rules = new ArrayList<>();

    public Rules() {
    }

    public void handle(List<Update> updateList) {
        for (Update update : updateList) {
            for (Rule rule : rules) {
                try {
                    if(update.callbackQuery() != null){
                        rule.callback(update.callbackQuery());
                    } else {
                        rule.run(update);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerRule(Rule rule) {
        rules.add(rule);
    }
}

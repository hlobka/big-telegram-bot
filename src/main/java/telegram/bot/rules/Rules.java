package telegram.bot.rules;

import com.pengrad.telegrambot.model.Update;
import helper.logger.ConsoleLogger;

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
                        rule.callback(update);
                    } else {
                        if (rule.guard(update)) {
//                            String simpleName = rule.getClass().getSimpleName();
//                            System.out.println(simpleName);
                            rule.run(update);
                        }
                    }
                } catch (Exception e){
                    ConsoleLogger.logErrorFor(this, e);
                }
            }
        }
    }

    public void registerRule(Rule rule) {
        rules.add(rule);
    }
}

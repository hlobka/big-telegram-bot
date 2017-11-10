package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.commons.lang.ArrayUtils;
import telegram.bot.data.Common;

import java.util.HashMap;
import java.util.Map;

public class IIAnswerRule implements Rule {
    private final TelegramBot bot;
    //    private final Answer answer = new Answer();
    private final Map<String, Answer> answers = new HashMap<>();

    public IIAnswerRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        if (message.chat().id() == Common.TEST_FOR_BOT_GROUP_ID) {
            if(true) return;
            if (message.replyToMessage() != null) {
                Answer answer = null;
                for (Map.Entry<String, Answer> entry : answers.entrySet()) {
                    if (answer == null || answer.coast < entry.getValue().coast) {
                        answer = entry.getValue();
                    }
                }
                if (answer == null) {
                    return;
                }
                String strAnswer = answer.getAnswer();
                SendMessage request = new SendMessage(message.chat().id(), strAnswer)
                    .parseMode(ParseMode.Markdown)
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
                return;
            }


            String[] split = text.split(" ");
            for (int i = 0; i < split.length; i++) {
                Answer answer;
                String key = split[i];
                if (!answers.containsKey(key)) {
                    answers.put(key, new Answer(key));
                }
                answer = answers.get(key);
                answer.setAnswers((String[]) ArrayUtils.subarray(split, i + 1, split.length));
            }
        }
    }

    private class Answer {
        public Map<String, Answer> answers = new HashMap<>();
        public Integer coast = 0;
        private String name;

        public Answer(String name) {

            this.name = name;
        }

        public void setAnswers(String[] split) {
            for (int i = 0; i < split.length; i++) {
                Answer answer;
                String key = split[i];
                if (!answers.containsKey(key)) {
                    answers.put(key, new Answer(key));
                }
                answer = answers.get(key);
                if (split.length > 1) {
                    answer.setAnswers((String[]) ArrayUtils.subarray(split, i + 1, split.length));
                }
                coast += 1;
            }
        }

        public String getAnswer() {
            String result = name;
            Answer answer = null;
            for (Map.Entry<String, Answer> entry : answers.entrySet()) {
                if (answer == null || answer.coast < entry.getValue().coast) {
                    answer = entry.getValue();
                }
            }
            if (answer != null) {
                result += " " + answer.getAnswer();
            }
            return result;
        }
    }
}

package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.commons.lang.ArrayUtils;
import telegram.bot.data.Common;
import telegram.bot.helper.MessageHelper;

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
        Message message = MessageHelper.getAnyMessage(update);
        if (message == null) {
            return;
        }
        String text = message.text() == null ? "" : message.text();
        if (message.chat().id() == Common.data.getChatForReport().get(0).getChatId() && !message.from().isBot()) {
            if(true) return;
            Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                new String[]{"first row button1", "first row button2"},
                new String[]{"second row button1", "second row button2"})
                .oneTimeKeyboard(true)   // optional
                .resizeKeyboard(true)    // optional
                .selective(true);        // optional
            SendMessage request2 = new SendMessage(message.chat().id(), "text")
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .replyToMessageId(message.messageId())
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                    new InlineKeyboardButton("just pay").callbackData(")pay("),
                    new InlineKeyboardButton("google it").callbackData("www.google.com")
                }));
            bot.execute(request2);
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

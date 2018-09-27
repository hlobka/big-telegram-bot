package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.SendResponse;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import joke.JokesProvider;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;
import telegram.bot.helper.ActionItemsHelper;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.StringMath;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;

public class AnswerRule implements Rule {
    private final TelegramBot bot;
    private Map<String, MessageSupplier<String>> commonRegAnswers = new HashMap<>();
    private Map<String, Function<String, String>> commonAnswers = new HashMap<>();
    private Map<String, Function<String, String>> answers = new HashMap<>();
    private Map<String, Function<Message, String>> actions = new HashMap<>();
    private List<String> popularBotAnswers = Arrays.asList(
        "Какие наркотики",
        "Кто тут?",
        "Lorem ipsum",
        "Пробельчики",
        "чай, кофе, потанцуем?",
        "Все так говорят, а ты купи Слона"
    );

    public AnswerRule(TelegramBot bot) {
        this.bot = bot;
        answers.put("бот, привет", s -> "О, Привет!");
        commonRegAnswers.put("бот,? голос", s -> {
            List<String> strings = Arrays.asList("Аф, Аф!!", "Миаууу", "Пффф...", "ква-ква", "кря-кря", "Квооо-ко-ко-к-ко", "и-О-а-Аа Эи эи эии", "ква-ква", "Ым Ым", "ЫЫ-ЫЫ", "пых-пых", "ту-ту", "пи-пи-пи", "Ня-ня-ня");
            return strings.get((int) Math.round(Math.random() * (strings.size() - 1)));
        });
        commonRegAnswers.put("бот,?.* анекдот\\??", MessageSupplier.getAs(ParseMode.HTML, s -> BotHelper.clearForHtmlMessages(new JokesProvider().provideNextUniqueJoke(100))));
        answers.put("как дела?", s -> "Да не плохо!\ncам как?");
        answers.put("это кто?", s -> "Я тот кто может тебе многое рассказать. \nЖми сюда /help");
        answers.put("кто это?", s -> "Я тот кто может тебе многое рассказать. \nЖми сюда /help");
        answers.put("наркотики", s -> "Какие наркотики?");
        answers.put("кайф", s -> "Какие наркотики?");
        answers.put("drugs", s -> "Какие наркотики?");
        answers.put("баги", s -> "это не баги, это фичи");
        answers.put("нудануда", s -> "Женя, это ты?");
        answers.put("рофл", s -> "Рома, это ты?");
        commonRegAnswers.put("ну почему.*\\?", s -> "Потому");
        answers.put("c'est la ", s -> "Женя, это ты?");
        answers.put("хех", s -> "Женя, это ты?");
        answers.put("хэх", s -> "Женя, это ты?");
        answers.put("хэг", s -> "Женя, это ты?");
        answers.put("годный апдейт", s -> "Женя, это ты?");
        answers.put("Хорошая практика", s -> "Женя, это ты?");
        answers.put("Ctrl+C", s -> "Не самая лучша практика");
        answers.put("Ctrl+V", s -> "Не самая лучша практика");
        answers.put("ctrl+c, ctrl+v", s -> "Не самая лучша практика");
        answers.put("ctrl + c, ctrl + v", s -> "Не самая лучша практика");
        answers.put("ctrl + c", s -> "Не самая лучша практика");
        answers.put("ctrl + v", s -> "Не самая лучша практика");
        answers.put("ctrl+v", s -> "Не самая лучша практика");
        answers.put("ctrl+c", s -> "Не самая лучша практика");
        answers.put("было бы не плохо", s -> "заведи экшин айтем");
        answers.put("не плохо", s -> "");
        answers.put("очень плохо", s -> "хуже некуда");
        answers.put("плохо", s -> "бывает и хуже");
        commonRegAnswers.put("куда хуже .*?", s -> "есть куда...");
        answers.put("домой", s -> "не рановато ли?");
        answers.put("кушать", s -> "пару минут, мне тут надо до перепроверить");
//        answers.put("курить", s -> "здоровью вредить");
//        answers.put("покурим", s -> "здоровью повредим");
        answers.put("идем", s -> "куда?");
        answers.put("минутку", s -> "ага, как всегда");
        answers.put("чай", s -> "кофе");
        answers.put("кофе", s -> "чай");
        answers.put("пиво", s -> "водка");
        answers.put("водка", s -> "пиво");
        answers.put("педалить", s -> "не лучшая практика в девелопменте");
//        answers.put("ревью", s -> "О, ревью, Набегай!");
        answers.put("в смысле?", s -> "В прямом");
        answers.put("Lorem ipsum", s -> "https://ru.wikipedia.org/wiki/Lorem_ipsum");
        commonRegAnswers.put("бимба", s -> "Это не я!!!");
        commonRegAnswers.put("заминировали", s -> "бимба!!!");
        commonRegAnswers.put("купить ([a-zA-Zа-яА-Я ]?)+лотерейку\\?", s -> {
            switch (new Random().nextInt(5)) {
                case 0:
                    return "Да";
                case 2:
                    return "Нет";
                case 3:
                    return "Лучше две";
                case 4:
                    return "Нивкоем случае";
                case 5:
                    return "Возможно";
            }
            return "Сегодня не ваш день...";
        });
        commonAnswers.put("Бот, как тебе ", s -> {
            String who = StringHelper.getRegString(s, "Бот, как тебе (моя?и? )?([А-Яа-яa-zA-Z ]+)\\?", 2);
            String which = "классная и красивая";
            if (who.substring(who.length() - 1).matches("[бвгджзйклмнпрстфхцчшщ]")) {
                which = "классный и красивый";
            }
            if (who.substring(who.length() - 1).matches("[ыЫиИ]")) {
                which = "классные и красивые";
            }
            int nextInt = new Random().nextInt(100);
            if (nextInt > 90) {
                return "одобряю";
            }
            if (nextInt > 10 && nextInt < 20) {
                return "ржунемагу";
            } else if (nextInt < 10) {
                return "ну такое";
            }
            return "Ну очень " + which + " " + who;
        });
        commonRegAnswers.put("да здравству(е|ю)т,? .*", s -> {
            String who = StringHelper.getRegString(s, "да здравству(е|ю)т,? ?([А-Яа-яa-zA-Z ]+)", 2);
            who = who.replaceAll("(\\W+)(а$)", "$1у");
            who = who.replaceAll("(\\W+)(я$)", "$1ю");
            who = who.replaceAll("(\\W+)(ь$)", "$1я");
            who = who.replaceAll("(\\W+)([бвгджзйклмнпрстфхцчшщ]$)", "$1$2а");
            return "Боже, Храни " + who + "!!!";
        });
        commonRegAnswers.put("бот, (сколько|скока) (\\W+) в ([ a-zA-ZА-Яа-я]+) ?\\??$", s -> {
            String regexp = "бот, (сколько|скока) (\\W+) в ([ a-zA-ZА-Яа-я]+) ?\\??$";
            String regString1 = StringHelper.getRegString(s.toLowerCase(), regexp, 3);
            String regString2 = StringHelper.getRegString(s.toLowerCase(), regexp, 2);
            Long random1 = Math.round(Math.random() * 10);
            Long random2 = Math.round(Math.random() * 10);
            return String.format("Я бы сказал что в %s %d %s но может и %d %s", regString1, random1, regString2, random2, regString2);
        });
        commonAnswers.put("Понедельник", s -> "День потерянного контекста");
        commonAnswers.put("Вторник", s -> "День говна");
        commonAnswers.put("Среда", s -> "Рунглишь дэй");
        commonAnswers.put("Четверг", s -> "День шаринга или несбывшегося пива");
        commonAnswers.put("Пятница", s -> "Какие наркотики");
        commonRegAnswers.put(".*(среду|пятницу).*", s -> Math.random() > 0.5 ? "не лучший день" : "лучше на пиво в этот день");
        commonRegAnswers.put(".*(срал|срать|дерьмо|говно|воня|понос).*", s -> {
            if (TimeHelper.checkToDayIs(DayOfWeek.TUESDAY)) {
                return "Как ни как Вторник";
            }
            return "Сегодня ж не вторник";
        });
        commonRegAnswers.put("бот, дай пять.*", s -> "✋️");
        commonRegAnswers.put("бот, дай один.*", s -> "🖕");
        commonRegAnswers.put("бот, дай два.*", s -> "🖕🖕");
        commonRegAnswers.put("бот, дай три.*", s -> "🖕🖕🖕");
        commonRegAnswers.put("бот, дай четыри.*", s -> "🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай шесть.*", s -> "🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай семь.*", s -> "🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай восемь.*", s -> "🖕🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай девять.*", s -> "🖕🖕🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай десять.*", s -> "✋✋️");
        commonRegAnswers.put("бот, (сколько|скока) у (\\W+) ([a-zA-ZА-Яа-я]+) ?\\??$", s -> {
            String regexp = "бот, (сколько|скока) у (\\W+) ([a-zA-ZА-Яа-я]+) ?\\??$";
            String regString1 = StringHelper.getRegString(s, regexp, 2);
            String regString2 = StringHelper.getRegString(s.toLowerCase(), regexp, 3);
            Long random1 = Math.round(Math.random() * 10);
            Long random2 = Math.round(Math.random() * 10);
            return String.format("Я бы сказал что у %s %d %s но может и %d %s", regString1, random1, regString2, random2, regString2);
        });
        commonRegAnswers.put("\\*+ ?ets ?\\*+", MessageSupplier.getAs(ParseMode.HTML, s -> EtsClarityChecker.getMessage(bot)));
        commonRegAnswers.put("\\*+ ?clarity ?\\*+", s -> "Все просто, заходим и заполняем\nhttps://clarity.gtk.gtech.com:8043/niku/nu#action:npt.overview");
        commonAnswers.put("хуй", s -> "Попрошу не матюкаться.");
        commonAnswers.put("пизд", s -> "Попрошу не матюкаться.");
        commonAnswers.put("ебат", s -> "Попрошу не матюкаться.");
        commonAnswers.put("ебан", s -> "Попрошу не матюкаться.");
        commonAnswers.put("бля", s -> "Попрошу не матюкаться.");
        commonAnswers.put("сволочь", s -> "Попрошу не выражаться.");
        commonAnswers.put("поц", s -> "Попрошу не выражаться.");
        commonAnswers.put("придур", s -> "Попрошу не выражаться.");
        commonAnswers.put("дура", s -> "Попрошу не выражаться.");
        commonAnswers.put("тупая", s -> "Попрошу не выражаться.");
        commonAnswers.put("тупой", s -> "Попрошу не выражаться.");
        commonRegAnswers.put("^[a-zа-я]{21,}$", s -> "Ну очень длинное слово");
        commonRegAnswers.put("(.*(Ф|ф)а+к,? .*)|(^(Ф|ф)а+к!{0,})", s -> "Попрошу не выражаться.");
        commonRegAnswers.put("^(M|m)erde$", s -> "Pue");
        commonRegAnswers.put("^[(0-9+ -/*^]{3,}$|^[(0-9+ -/*^)(]{5,}$", s -> {
            try {
                return StringMath.stringToMathResult(s) + "";
            } catch (NumberFormatException e) {
                return "NaN";
            }
        });
        commonAnswers.put("который час?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("сколько время?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("сколько времени?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("что такое ", s -> {
            String query = StringHelper.getRegString(s, "что такое ([a-zA-Zа-яА-Я ]+)\\??", 1);
            return Common.GOOGLE.getFirstResult(query);
        });
        commonRegAnswers.put("кто так(ой|ая),? .*", s -> {
            String query = StringHelper.getRegString(s, "кто так(ой|ая),? ([a-zA-Zа-яА-Я ]+)\\??", 2);
            return Common.GOOGLE.getFirstResult(query);
        });

        actions.put("#AI", message -> {
            String text = message.text() == null ? "" : message.text();
            StringBuilder result = new StringBuilder();
            Integer i = 0;
            Message reply = message.replyToMessage();
            if (reply != null) {
                if (reply.from().isBot()) {
                    return "Бот не может навязывать нам ActionItems";
                }
                int key = ActionItemsHelper.unresolved.saveActionItem(reply.text(), message.chat().id());
                result.append("Сохранил ActionItem\n")
                    .append("Вы можете закрыть его используя комманду: ")
                    .append("/resolveAI__").append(key);
                return result.toString();
            }
            for (String actionItem : text.split("#(AI|ai|Ai|aI) ")) {
                if (actionItem.isEmpty()) {
                    continue;
                }
                int key = ActionItemsHelper.unresolved.saveActionItem(actionItem, message.chat().id(), i);
                if (i > 0) {
                    result.append("\n");
                }
                result.append("Сохранил ActionItem\n")
                    .append("Вы можете закрыть его используя комманду: ")
                    .append("/resolveAI__").append(key);
                i++;
            }
            return result.toString();
        });
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        if (message.from().isBot()) {
            return;
        }
        Long chatId = message.chat().id();
        if (message.replyToMessage() != null) {
            if (message.replyToMessage().from().isBot()) {
                Collections.shuffle(popularBotAnswers);
                String answer = popularBotAnswers.get(0);
                SendMessage request = new SendMessage(chatId, answer)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
            }
        }
        if (text.toLowerCase().matches(".*(nikola|никола|николы|николой).*")) {
            runNikolaFeedBack(chatId);
        }
        User[] newChatMembers = message.newChatMembers();
        if (newChatMembers != null && newChatMembers.length > 0) {
            sendSticker(chatId, "CAADAgADiwAD8MPADg9RUg3DhE-TAg");
        }
        for (Map.Entry<String, Function<Message, String>> entry : actions.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey().toLowerCase())) {
                String messageFroAction = entry.getValue().apply(message);
                if (messageFroAction.isEmpty()) {
                    continue;
                }
                SendMessage request = new SendMessage(chatId, messageFroAction)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
            }
        }
        if (!BIG_GENERAL_GROUP_IDS.contains(chatId)) {
            for (Map.Entry<String, Function<String, String>> entry : answers.entrySet()) {
                if (entry.getKey().isEmpty()) {
                    break;
                }
                if (text.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    SendMessage request = new SendMessage(chatId, entry.getValue().apply(text))
                        .parseMode(ParseMode.Markdown)
                        .disableWebPagePreview(false)
                        .disableNotification(true)
                        .replyToMessageId(message.messageId());
                    bot.execute(request);
                    break;
                }
            }
        }
        for (Map.Entry<String, Function<String, String>> entry : commonAnswers.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey().toLowerCase())) {
                SendMessage request = new SendMessage(chatId, entry.getValue().apply(text))
                    .parseMode(ParseMode.Markdown)
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
                return;
            }
        }
        for (Map.Entry<String, MessageSupplier<String>> entry : commonRegAnswers.entrySet()) {
            if (text.toLowerCase().matches(entry.getKey())) {
                SendMessage request = new SendMessage(chatId, entry.getValue().apply(text))
                    .parseMode(entry.getValue().getParseMode())
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
                return;
            }
        }
    }

    private void runNikolaFeedBack(Long chatId) {
        new Thread(() -> {
            sendTemporarySticker(chatId, "CAADAgADDQADq3NqEqHyL5dZSXw6Ag");
            sendTemporarySticker(chatId, "CAADAgADDgADq3NqEufGSaMoFpp6Ag");
            sendTemporarySticker(chatId, "CAADAgADDwADq3NqEiR-KIzRQKwHAg");
        }).start();
    }

    private void sendTemporarySticker(Long chatId, String stickerId) {
        SendResponse sendResponse;
        sendResponse = sendSticker(chatId, stickerId);
        TimeHelper.waitTime(1, TimeUnit.SECONDS);
        bot.execute(new DeleteMessage(chatId, sendResponse.message().messageId()));
    }

    private SendResponse sendSticker(Long chatId, String stickerId) {
        return bot.execute(new SendSticker(chatId, stickerId).disableNotification(true));
    }

    private interface MessageSupplier<T> extends Function<T, String> {
        default ParseMode getParseMode() {
            return ParseMode.Markdown;
        }

        static <T> MessageSupplier getAs(ParseMode parseMode, MessageSupplier<T> messageSupplier) {
            return new MessageSupplier<T>() {
                @Override
                public String apply(T o) {
                    return messageSupplier.apply(o);
                }

                @Override
                public ParseMode getParseMode() {
                    return parseMode;
                }
            };
        }
    }
}

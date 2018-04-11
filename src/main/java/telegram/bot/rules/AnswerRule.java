package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import helper.string.StringHelper;
import telegram.bot.checker.EtsClarityChecker;
import telegram.bot.data.Common;
import telegram.bot.dto.ActionItemDto;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static telegram.bot.data.Common.ACTION_ITEMS2;
import static telegram.bot.data.Common.BIG_GENERAL_GROUP_IDS;
import static telegram.bot.data.Common.JOKE_ITEMS;

public class AnswerRule implements Rule {
    public static final String MATH_REG = "^(\\d+(\\.\\d+)?)( ?)+%s( ?)+(\\d+(\\.\\d+)?)$";
    public static final String MATH_PLUS = String.format(MATH_REG, "\\+");
    public static final String MATH_MINUS = String.format(MATH_REG, "\\-");
    public static final String MATH_MULTIPLY = String.format(MATH_REG, "\\*");
    public static final String MATH_DIVIDE = String.format(MATH_REG, "\\/");
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
        commonRegAnswers.put("бот,?.* анекдот\\??", s -> {
            ArrayList<String> popularBotJokes = SharedObject.loadList(JOKE_ITEMS);
            Collections.shuffle(popularBotJokes);
            return popularBotJokes.get(0);
        });
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
        answers.put("плохо", s -> "бывает и хуже");
        commonRegAnswers.put("куда хуже .*?", s -> "есть куда...");
//        answers.put("очень плохо", s -> "бывает и хуже");
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
        commonAnswers.put("Понедельник", s -> "День потеряного контекста");
        commonAnswers.put("Вторник", s -> "День говна");
        commonAnswers.put("Среда", s -> "Молчание Ромы");
        commonAnswers.put("Четверг", s -> "День шаринга или несбывшегося пива");
        commonAnswers.put("Пятница", s -> "Какие наркотики");
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
//        commonAnswers.put("* ETS *", s -> "Все просто, заходим и заполняем\nhttps://timeserver.i.sigmaukraine.com/timereports.ets");
//        commonRegAnswers.put("\\*+ ?ets ?\\*+", s -> "Все просто, заходим и заполняем\nhttps://timeserver.i.sigmaukraine.com/timereports.ets");
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
//        commonAnswers.put("фак ", s -> "Попрошу не выражаться.");
//        commonAnswers.put(" фак", s -> "Попрошу не выражаться.");
        commonAnswers.put("гавн", s -> "Попрошу не выражаться.");
        commonAnswers.put("говн", s -> "Попрошу не выражаться.");
        commonAnswers.put("тупая", s -> "Попрошу не выражаться.");
        commonAnswers.put("тупой", s -> "Попрошу не выражаться.");
        commonRegAnswers.put("^[a-zа-я]{21,}$", s -> "Ну очень длинное слово");
        commonRegAnswers.put("(.*(Ф|ф)а+к,? .*)|(^(Ф|ф)а+к!{0,})", s -> "Попрошу не выражаться.");
        commonRegAnswers.put("^(M|m)erde$", s -> "Pue");
        commonRegAnswers.put(MATH_PLUS, s -> {
            String regString1 = StringHelper.getRegString(s, MATH_PLUS, 1);
            String regString2 = StringHelper.getRegString(s, MATH_PLUS, 5);
            return Float.parseFloat(regString1) + Float.parseFloat(regString2) + "";
        });
        commonRegAnswers.put(MATH_MULTIPLY, s -> {
            String regString1 = StringHelper.getRegString(s, MATH_MULTIPLY, 1);
            String regString2 = StringHelper.getRegString(s, MATH_MULTIPLY, 5);
            return Float.parseFloat(regString1) * Float.parseFloat(regString2) + "";
        });
        commonRegAnswers.put(MATH_MINUS, s -> {
            String regString1 = StringHelper.getRegString(s, MATH_MINUS, 1);
            String regString2 = StringHelper.getRegString(s, MATH_MINUS, 5);
            return Float.parseFloat(regString1) - Float.parseFloat(regString2) + "";
        });
        commonRegAnswers.put(MATH_DIVIDE, s -> {
            String regString1 = StringHelper.getRegString(s, MATH_DIVIDE, 1);
            String regString2 = StringHelper.getRegString(s, MATH_DIVIDE, 5);
            float secondParam = Float.parseFloat(regString2);
            if (secondParam == 0) {
                return "NaN";
            }
            return Float.parseFloat(regString1) / secondParam + "";
        });
        commonAnswers.put("который час?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("сколько время?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("сколько времени?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("что такое ", s -> {
            String query = StringHelper.getRegString(s, "что такое ([a-zA-Zа-яА-Я ]+)\\??", 1);
            return Common.GOOGLE.getFirstResult(query);
        });
        commonAnswers.put("кто такой ", s -> {
            String query = StringHelper.getRegString(s, "кто такой ([a-zA-Zа-яА-Я ]+)\\??", 1);
            return Common.GOOGLE.getFirstResult(query);
        });

        actions.put("#AI", message -> {
            String text = message.text() == null ? "" : message.text();
            HashMap<Integer, ActionItemDto> actionItems2 = SharedObject.loadMap(ACTION_ITEMS2, new HashMap<Integer, ActionItemDto>());
            StringBuilder result = new StringBuilder();
            Integer i = 0;
            for (String actionItem : text.split("#(AI|ai|Ai|aI) ")) {
                if (actionItem.isEmpty()) {
                    continue;
                }
                String date = new SimpleDateFormat("dd.MM/HH:mm:ss").format(Calendar.getInstance().getTime());
                int key = Math.abs((date + i).hashCode());
                actionItems2.put(key, new ActionItemDto(date, actionItem, message.chat().id()));
                if (i > 0) {
                    result.append("\n");
                }
                result.append("Сохранил ActionItem\n")
                    .append("Вы можете закрыть его используя комманду: ")
                    .append("/resolveAI__").append(key);
                i++;
            }
            SharedObject.save(ACTION_ITEMS2, actionItems2);
            return result.toString();
        });
        Function<Message, String> jokesSaver = message -> {
            String text = message.text() == null ? "" : message.text();
            ArrayList<String> popularBotJokes = SharedObject.loadList(JOKE_ITEMS);
            for (String joke : text.split("#(анекдот|joke) ")) {
                if (joke.isEmpty()) {
                    continue;
                }
                popularBotJokes.add(joke);
            }
            SharedObject.save(JOKE_ITEMS, popularBotJokes);
            return "Jokes saved";
        };
        actions.put("#анекдот", jokesSaver);
        actions.put("#joke", jokesSaver);
        actions.put("#clearJokes",  message -> {
            SharedObject.save(JOKE_ITEMS, new ArrayList<String>());
            return "jokes cleared";
        });
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        if (message.from().isBot()) {
            return;
        }
        if (message.replyToMessage() != null) {
            if (message.replyToMessage().from().isBot()) {
                Collections.shuffle(popularBotAnswers);
                String answer = popularBotAnswers.get(0);
                SendMessage request = new SendMessage(message.chat().id(), answer)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
            }
        }
        for (Map.Entry<String, Function<Message, String>> entry : actions.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey().toLowerCase())) {
                SendMessage request = new SendMessage(message.chat().id(), entry.getValue().apply(message))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
            }
        }
        if (!BIG_GENERAL_GROUP_IDS.contains(message.chat().id())) {
            for (Map.Entry<String, Function<String, String>> entry : answers.entrySet()) {
                if (text.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    SendMessage request = new SendMessage(message.chat().id(), entry.getValue().apply(text))
                        .parseMode(ParseMode.Markdown)
                        .disableWebPagePreview(false)
                        .disableNotification(true)
                        .replyToMessageId(message.messageId());
                    bot.execute(request);
                }
            }
        }
        for (Map.Entry<String, Function<String, String>> entry : commonAnswers.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey().toLowerCase())) {
                SendMessage request = new SendMessage(message.chat().id(), entry.getValue().apply(text))
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
                SendMessage request = new SendMessage(message.chat().id(), entry.getValue().apply(text))
                    .parseMode(entry.getValue().getParseMode())
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
                bot.execute(request);
                return;
            }
        }
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

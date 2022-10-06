package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.SendResponse;
import helper.string.StringHelper;
import helper.time.TimeHelper;
import joke.JokesProvider;
import telegram.bot.data.Common;
import telegram.bot.helper.ActionItemsHelper;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.MessageHelper;
import telegram.bot.helper.StringMath;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AnswerRule implements Rule {
    private final TelegramBot bot;
    private Map<Long, MessageSupplier<String>> nextChatAnswer = new HashMap<>();
    private Map<String, MessageSupplier<String>> commonRegAnswers = new HashMap<>();
    private Map<String, Function<String, String>> commonAnswers = new HashMap<>();
    private Map<String, Function<String, String>> answers = new HashMap<>();
    private Map<String, Function<Message, String>> actions = new HashMap<>();
    private List<String> popularBotAnswers = Arrays.asList(
        "Ох і гарні новини будуть, чекайте",
        "Так, я буду знущатись з русні по максимуму сьогодні",
        "ЗСУ ЗСУ ЗСУ",
        "Русні п;зда",
        "на смерть русні 6 грн \uD83D\uDC79",
        "Все буде добре",
        "Які наркотики",
        "Хто тут?",
        "Lorem ipsum",
        "Пробільчики",
        "чай, кава, потанцуємо?",
        "Всі так говорять, а ты купи Слона"
    );

    public AnswerRule(TelegramBot bot) {
        this.bot = bot;
        answers.put("Повітряна тривога", s -> "Всі в укриття");
        answers.put("бот, привет", s -> "О, Привет!");
        answers.put("бот, привіт", s -> "О, Привет!");
        answers.put("да, бот?", s -> {
            nextChatAnswer.put(-1L, s1 -> "Я в раздумиях...");
            return "Что да?";
        });
        answers.put("так, бот?", s -> {
            nextChatAnswer.put(-1L, s1 -> "Я в раздумах...");
            return "Що так?";
        });
        commonRegAnswers.put("бот,? голос", s -> {
            List<String> strings = Arrays.asList("Аф, Аф!!", "Миаууу", "Пффф...", "ква-ква", "кря-кря", "Квооо-ко-ко-к-ко", "и-О-а-Аа Эи эи эии", "ква-ква", "Ым Ым", "ЫЫ-ЫЫ", "пых-пых", "ту-ту", "пи-пи-пи", "Ня-ня-ня");
            return strings.get((int) Math.round(Math.random() * (strings.size() - 1)));
        });
        JokesProvider jokesProvider = new JokesProvider("anekdot.ru", "new%20anekdot");
        commonRegAnswers.put("бот,?.* анекдот\\??", MessageSupplier.getAs(ParseMode.HTML, s -> BotHelper.clearForHtmlMessages(jokesProvider.provideNextUniqueJoke(100))));
        answers.put("як справи?", s -> "Та норм!\ncам як?");
        answers.put("как дела?", s -> "Да не плохо!\ncам как?");
        answers.put("це кто?", s -> "Я той що може тобі багато що розповісти. \nТисни /help");
        answers.put("это кто?", s -> "Я тот кто может тебе многое рассказать. \nЖми сюда /help");
        answers.put("кто это?", s -> "Я тот кто может тебе многое рассказать. \nЖми сюда /help");
        answers.put("наркотики", s -> "Які наркотики?");
        answers.put("кайф", s -> "Какие наркотики?");
        answers.put("drugs", s -> "Какие наркотики?");
        answers.put("баги", s -> "это не баги, это фичи");
        commonRegAnswers.put("ну чому.*\\?", s -> "Потому");
        commonRegAnswers.put("ну почему.*\\?", s -> "Потому");
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
        answers.put("плохо", s -> "буває гірше");
        commonRegAnswers.put("куда хуже .*?", s -> "есть куда...");
        commonRegAnswers.put("куди шірше .*?", s -> "є куди...");
        answers.put("домой", s -> "не рановато ли?");
        answers.put("додому", s -> "не рановато ли?");
        answers.put("їсти", s -> "пару хвилин, мені тут треба до перевірити");
        answers.put("кушать", s -> "пару минут, мне тут надо до перепроверить");
//        answers.put("курить", s -> "здоровью вредить");
//        answers.put("покурим", s -> "здоровью повредим");
        answers.put("йдем", s -> "куда?");
        answers.put("идем", s -> "куда?");
        answers.put("хвилинку", s -> "ага, як завжди");
        answers.put("минутку", s -> "ага, как всегда");
        answers.put("чай", s -> "кава");
        answers.put("кофе", s -> "чай");
        answers.put("кава", s -> "чай");
        answers.put("пиво", s -> "горілка");
        answers.put("водка", s -> "самогон");
        answers.put("самогон", s -> "горілка");
        answers.put("горілка", s -> "пиво");
        answers.put("педалить", s -> "не лучшая практика в девелопменте");
//        answers.put("ревью", s -> "О, ревью, Набегай!");
        answers.put("в смысле?", s -> "В прямом");
        answers.put("Lorem ipsum", s -> "https://ru.wikipedia.org/wiki/Lorem_ipsum");
        commonRegAnswers.put("бимба", s -> "Это не я!!!");
        commonRegAnswers.put("заминировали", s -> "бимба!!!");
        commonRegAnswers.put("замінували", s -> "бимба!!!");
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
        commonAnswers.put("Гадицца", s -> "Не обГадицца");
        List<String> possibleAnswers = Arrays.asList("Я в раздумиях...","Я Тоже так думал...", "А ты сам попробуй...");
        commonRegAnswers.put(".*брос(ила|ал|ил|ать|им|ай).*", s -> {
            nextChatAnswer.put(-1L, s1 -> {
                Collections.shuffle(possibleAnswers);
                nextChatAnswer.put(-1L, s2 -> {
                    Collections.shuffle(possibleAnswers);
                    return possibleAnswers.get(0);
                });
                return possibleAnswers.get(0);
            });
            return "Я как то курить бросал...";
        });
        commonAnswers.put("Он в отпуске.*", s -> "Так ему и надо, Заслужил!");
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
        commonAnswers.put("Бот, як тобі ", s -> {
            String who = StringHelper.getRegString(s, "Бот, як тобі (моя?ї? )?([А-Яа-яa-zA-Z ]+)\\?", 2);
            String which = "классна та красива";
            if (who.substring(who.length() - 1).matches("[бвгджзйклмнпрстфхцчшщ]")) {
                which = "чудовий та красивий";
            }
            if (who.substring(who.length() - 1).matches("[ыЫиИІіЯя]")) {
                which = "чудові та красиві";
            }
            int nextInt = new Random().nextInt(100);
            if (nextInt > 90) {
                return "в захваті";
            }
            if (nextInt > 10 && nextInt < 20) {
                return "ржунеможу";
            } else if (nextInt < 10) {
                return "ну таке";
            }
            return "Ну дуже " + which + " " + who;
        });
        commonRegAnswers.put("да здравству(е|ю)т,? .*", s -> {
            String who = StringHelper.getRegString(s, "да здравству(е|ю)т,? ?([А-Яа-яa-zA-Z ]+)", 2);
            who = who.replaceAll("(\\W+)(а$)", "$1у");
            who = who.replaceAll("(\\W+)(я$)", "$1ю");
            who = who.replaceAll("(\\W+)(ь$)", "$1я");
            who = who.replaceAll("(\\W+)([бвгджзйклмнпрстфхцчшщ]$)", "$1$2а");
            return "Боже, Храни " + who + "!!!";
        });
        commonRegAnswers.put("да здравству(є|ю)??,? .*", s -> {
            String who = StringHelper.getRegString(s, "да здравству(є|ю)??,? ?([А-Яа-яa-zA-ZїЇ'іІ ]+)", 2);
            who = who.replaceAll("(\\W+)(а$)", "$1у");
            who = who.replaceAll("(\\W+)(я$)", "$1ю");
            who = who.replaceAll("(\\W+)(ь$)", "$1я");
            who = who.replaceAll("(\\W+)([бвгджзйклмнпрстфхцчшщ]$)", "$1$2а");
            return "Боже, Бережи " + who + "!!!";
        });
        commonRegAnswers.put("бот, (скільки|кіки) (\\W+) в ([ a-zA-ZА-Яа-я]+) ?\\??$", s -> {
            String regexp = "бот, (скільки|кіки) (\\W+) в ([ a-zA-ZА-Яа-я]+) ?\\??$";
            String regString1 = StringHelper.getRegString(s.toLowerCase(), regexp, 3);
            String regString2 = StringHelper.getRegString(s.toLowerCase(), regexp, 2);
            Long random1 = Math.round(Math.random() * 10);
            Long random2 = Math.round(Math.random() * 10);
            return String.format("Я б сказав що у %s %d %s но може й %d %s", regString1, random1, regString2, random2, regString2);
        });
        commonRegAnswers.put("бот, (скільки|кіки) у (\\W+) ([a-zA-ZА-Яа-я]+) ?\\??$", s -> {
            String regexp = "бот, (скільки|кіки) у (\\W+) ([a-zA-ZА-Яа-я]+) ?\\??$";
            String regString1 = StringHelper.getRegString(s, regexp, 2);
            String regString2 = StringHelper.getRegString(s.toLowerCase(), regexp, 3);
            Long random1 = Math.round(Math.random() * 10);
            Long random2 = Math.round(Math.random() * 10);
            return String.format("Я бы сказав що у %s %d %s но може й %d %s", regString1, random1, regString2, random2, regString2);
        });
        commonRegAnswers.put("бот, (сколько|скока) (\\W+) в ([ a-zA-ZА-Яа-я]+) ?\\??$", s -> {
            String regexp = "бот, (сколько|скока) (\\W+) в ([ a-zA-ZА-Яа-я]+) ?\\??$";
            String regString1 = StringHelper.getRegString(s.toLowerCase(), regexp, 3);
            String regString2 = StringHelper.getRegString(s.toLowerCase(), regexp, 2);
            Long random1 = Math.round(Math.random() * 10);
            Long random2 = Math.round(Math.random() * 10);
            return String.format("Я бы сказал что в %s %d %s но может и %d %s", regString1, random1, regString2, random2, regString2);
        });
        commonRegAnswers.put("бот, (сколько|скока) у (\\W+) ([a-zA-ZА-Яа-я]+) ?\\??$", s -> {
            String regexp = "бот, (сколько|скока) у (\\W+) ([a-zA-ZА-Яа-я]+) ?\\??$";
            String regString1 = StringHelper.getRegString(s, regexp, 2);
            String regString2 = StringHelper.getRegString(s.toLowerCase(), regexp, 3);
            Long random1 = Math.round(Math.random() * 10);
            Long random2 = Math.round(Math.random() * 10);
            return String.format("Я бы сказал что у %s %d %s но может и %d %s", regString1, random1, regString2, random2, regString2);
        });
        answers.put("Понеділок", s -> "День загубленго контексту");
        answers.put("Понедельник", s -> "День потерянного контекста");
        answers.put("Вівторок", s -> "День гівна");
        answers.put("Вторник", s -> "День говна");
        answers.put("Середа", s -> "День англійскої мови");
        answers.put("Среда", s -> "Рунглишь дэй");
        answers.put("Четвер", s -> "День шарінга або нездійсненого пива");
        answers.put("Четверг", s -> "День шаринга или несбывшегося пива");
        answers.put("П'ятница", s -> "Які наркотики");
        answers.put("Пятница", s -> "Какие наркотики");
        answers.put(".*(среду|пятницу).*", s -> Math.random() > 0.5 ? "не лучший день" : "лучше на пиво в этот день");
        answers.put(".*(срал|срать|дерьмо|говно|воня|понос).*", s -> {
            if (TimeHelper.checkToDayIs(DayOfWeek.TUESDAY)) {
                return "Как ни как Вторник";
            }
            return "Сегодня ж не вторник";
        });
        answers.put(".*(срав|срати|гівно|лайно|воня|пронос).*", s -> {
            if (TimeHelper.checkToDayIs(DayOfWeek.TUESDAY)) {
                return "Як ні як Вівторок";
            }
            return "Сйогодня ж не вівторок";
        });
        commonRegAnswers.put("бот, дай п'ять.*", s -> "✋️");
        commonRegAnswers.put("бот, дай пять.*", s -> "✋️");
        commonRegAnswers.put("бот, дай один.*", s -> "🖕");
        commonRegAnswers.put("бот, дай два.*", s -> "🖕🖕");
        commonRegAnswers.put("бот, дай три.*", s -> "🖕🖕🖕");
        commonRegAnswers.put("бот, дай четыри.*", s -> "🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай чотири.*", s -> "🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай шість.*", s -> "🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай шесть.*", s -> "🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай сімь.*", s -> "🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай семь.*", s -> "🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай вісемь.*", s -> "🖕🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай восемь.*", s -> "🖕🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай дев'ять.*", s -> "🖕🖕🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай девять.*", s -> "🖕🖕🖕🖕🖕🖕🖕🖕🖕");
        commonRegAnswers.put("бот, дай десять.*", s -> "✋✋️");

        commonAnswers.put("Ковид", s -> "Будь те здоровы");
        commonAnswers.put("Ковід", s -> "Будь те здорові");
        commonAnswers.put("Covid", s -> "Одевайте маску");
        commonAnswers.put("путин", s -> "хуйло");
        commonAnswers.put("путін", s -> "хуйло");
        commonAnswers.put("хуй", s -> "Попрошу не матюкатися.");
        commonAnswers.put("пизд", s -> "Попрошу не матюкатися.");
        commonAnswers.put("ебат", s -> "Попрошу не матюкатися.");
        commonAnswers.put("їбат", s -> "Попрошу не матюкатися.");
        commonAnswers.put("їбош", s -> "Попрошу не матюкатися.");
        commonAnswers.put("ебан", s -> "Попрошу не матюкатися.");
        commonAnswers.put("бля", s -> "Попрошу не матюкатися.");
        commonAnswers.put("жопа", s -> "Буває і гірше...");
        commonAnswers.put("сволочь", s -> "Попрошу не висловлюватися.");
        commonAnswers.put("поц", s -> "Попрошу не висловлюватися.");
        commonAnswers.put("придур", s -> "Попрошу не висловлюватися.");
        commonAnswers.put("дура", s -> "Попрошу не выражаться.");
        commonAnswers.put("тупая", s -> "Попрошу не висловлюватися.");
        commonAnswers.put("тупой", s -> "Попрошу не висловлюватися.");
        commonAnswers.put("охуенные", s -> "Лучше некуда!!!.");
        commonAnswers.put("охуєнні", s -> "Лучше некуда!!!.");
        commonAnswers.put("Русні", s -> "Пізда!!!.");
        commonAnswers.put("Русні?", s -> "Пізда!!!.");
        commonAnswers.put("Слава Україні", s -> "Героям слава!!!.");
        commonAnswers.put("Слава нації", s -> "Смерть ворогам!!!.");
        commonAnswers.put("Україна", s -> "Понад Усе!!!.");
        commonRegAnswers.put("^[a-zа-я]{21,}$", s -> "Ну очень длинное слово");
        commonRegAnswers.put("(.*(Ф|ф)а+к,? .*)|(^(Ф|ф)а+к!{0,})", s -> "Попрошу не выражаться.");
        commonRegAnswers.put("^(M|m)erde$", s -> "Pue");
        commonRegAnswers.put("^\\){7,}$", s -> "Что за губная гормошка?");
        commonRegAnswers.put(StringMath.REG_EXPRESSION_TO_MATH_MATCH, s -> {
            try {
                return StringMath.stringToMathResult(s) + "";
            } catch (NumberFormatException e) {
                return "NaN";
            }
        });
        commonAnswers.put("Яка година?", s -> String.format("Зараз біля: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("который час?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("сколько время?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("сколько времени?", s -> String.format("Сейчас около: %s", new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())));
        commonAnswers.put("що таке ", s -> {
            String query = StringHelper.getRegString(s, "що таке ([a-zA-Zа-яА-ЯЇїІі' ]+)\\??", 1);
            return Common.GOOGLE.getFirstResult(query);
        });
        commonAnswers.put("что такое ", s -> {
            String query = StringHelper.getRegString(s, "что такое ([a-zA-Zа-яА-ЯЇїІі' ]+)\\??", 1);
            return Common.GOOGLE.getFirstResult(query);
        });
        commonRegAnswers.put("кто так(ой|ая),? .*", s -> {
            String query = StringHelper.getRegString(s, "кто так(ой|ая),? ([a-zA-Zа-яА-ЯЇїІі' ]+)\\??", 2);
            return Common.GOOGLE.getFirstResult(query);
        });
        commonRegAnswers.put("хто так(ий|ая),? .*", s -> {
            String query = StringHelper.getRegString(s, "хто так(ий|ая),? ([a-zA-Zа-яА-ЯЇїІі' ]+)\\??", 2);
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
    public boolean guard(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        boolean isBot = message.from() != null && message.from().isBot();
        return !isBot;
    }

    @Override
    public void run(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        if (message == null) {
            return;
        }
        String text = message.text() == null ? "" : message.text();
        Long chatId = message.chat().id();
        if (message.replyToMessage() != null && message.replyToMessage().from() != null) {
            if (message.replyToMessage().from().isBot() && Common.data.isSpamChat(chatId)) {
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
        if (Common.data.isSpamChat(chatId)) {
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
                registerPossibleChatAnswer(chatId);
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
                registerPossibleChatAnswer(chatId);
                return;
            }
        }
        if(nextChatAnswer.containsKey(chatId)){
            MessageSupplier<String> stringMessageSupplier = nextChatAnswer.get(chatId);
            nextChatAnswer.remove(chatId);
            SendMessage request = new SendMessage(chatId, stringMessageSupplier.apply(text))
                    .parseMode(ParseMode.Markdown)
                    .disableWebPagePreview(false)
                    .disableNotification(true)
                    .replyToMessageId(message.messageId());
            bot.execute(request);
        }
        registerPossibleChatAnswer(chatId);
    }

    public void registerPossibleChatAnswer(Long chatId) {
        if(nextChatAnswer.containsKey(-1L)){
            nextChatAnswer.put(chatId, nextChatAnswer.get(-1L));
            nextChatAnswer.remove(-1L);
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
        BotHelper.removeMessage(bot, chatId, sendResponse.message().messageId());
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

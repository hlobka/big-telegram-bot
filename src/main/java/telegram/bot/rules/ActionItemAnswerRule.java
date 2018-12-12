package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import helper.logger.ConsoleLogger;
import helper.string.StringHelper;
import telegram.bot.data.Common;
import telegram.bot.helper.ActionItemsHelper;
import telegram.bot.helper.BotHelper;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActionItemAnswerRule implements Rule {
    private TelegramBot bot;

    public ActionItemAnswerRule(TelegramBot bot) {
        this.bot = bot;
    }

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(Common.data.token);
        Rules rules = new Rules();
        rules.registerRule(new ActionItemAnswerRule(bot));
        ConsoleLogger.additionalErrorLogger = message -> {
            BotHelper.logError(bot, message);
        };
        bot.setUpdatesListener(updatess -> {
            System.out.println("onResponse: " + updatess.toString());
            new Thread(() -> rules.handle(updatess)).start();
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text() == null ? "" : message.text();
        if (message.from().isBot()) {
            return;
        }
        if (text.toLowerCase().contains("#ai")) {
            Integer i = 0;
            Message reply = message.replyToMessage();
            if (reply != null) {
                if (reply.from().isBot()) {
                    String notification = "Бот не может навязывать нам ActionItems";
                    BotHelper.sendMessage(bot, message.chat().id(), notification, ParseMode.HTML);
                }
                int key = ActionItemsHelper.unresolved.saveActionItem(reply.text(), message.chat().id());
                ActionItemsHelper.unresolved.askForDeadLine(bot, key, message);
                return;
            }
            for (String actionItem : text.split("#(AI|ai|Ai|aI) ")) {
                if (actionItem.isEmpty()) {
                    continue;
                }
                int key = ActionItemsHelper.unresolved.saveActionItem(actionItem, message.chat().id(), i);
                ActionItemsHelper.unresolved.askForDeadLine(bot, key, message);
            }
        }
    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;

        if (isDataPresent) {
            Message message = callbackQuery.message();
            if (message != null) {
                String data = callbackQuery.data();
                Long chatId = callbackQuery.message().chat().id();
                Integer messageId = callbackQuery.message().messageId();
                String regex = "aiDl_choose_(next|previous)_(year|month)__(\\d{4})_(\\d{1,2})__(.*)";
                if (data.matches(regex)) {
//                    BotHelper.removeMessage(bot, callbackQuery.message());
                    String actionItemKey = StringHelper.getRegString(data, regex, 5);
                    int monthNumber = Integer.parseInt(StringHelper.getRegString(data, regex, 4));
                    int yearNumber = Integer.parseInt(StringHelper.getRegString(data, regex, 3));
                    String command = StringHelper.getRegString(data, regex, 1);
                    String parameter = StringHelper.getRegString(data, regex, 2);
                    if (parameter.equals("month")) {
                        if (command.equals("next")) {
                            monthNumber += 1;
                            if (monthNumber > 12) {
                                monthNumber = 1;
                                yearNumber += 1;
                            }
                        } else {
                            monthNumber -= 1;
                            if (monthNumber <= 0) {
                                monthNumber = 12;
                                yearNumber -= 1;
                            }
                        }
                    } else {
                        if (command.equals("next")) {
                            yearNumber += 1;
                        } else {
                            yearNumber -= 1;
                        }
                    }
                    showDaysChooserCalendar(chatId, messageId, actionItemKey, yearNumber, monthNumber);
                } else if (data.contains("choose_aiDl")) {
                    BotHelper.removeMessage(bot, callbackQuery.message());
                    String actionItemKey = data.replace("choose_aiDl_", "");
                    showDaysChooserCalendar(chatId, actionItemKey);

                } else if (data.contains("avoid_ai_dead_line_")) {
                    String actionItemKey = data.replace("avoid_ai_dead_line_", "");
                    String text = "Сохранил ActionItem\nВы можете закрыть его используя комманду: /resolveAI__" + actionItemKey;
                    BotHelper.removeMessage(bot, callbackQuery.message());
                    BotHelper.sendMessage(bot, chatId, text, ParseMode.HTML);
                }

            }
        }
    }

    private void showHoursCalendar(Long chatId, String actionItemKey) {
        SendMessage request = new SendMessage(chatId, "Choose hour of day: ")
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .disableNotification(false)
                .replyMarkup(getReplyMarkupControls(HourOfDay.values(), actionItemKey));
        bot.execute(request);
    }

    private void showDaysChooserCalendar(Long chatId, String actionItemKey) {
        showDaysChooserCalendar(chatId, -1, actionItemKey);
    }

    private void showDaysChooserCalendar(Long chatId, int messageId, String actionItemKey) {
        Calendar calendar = Calendar.getInstance();
        int monthNumber = calendar.get(Calendar.MONTH);
        int yearNumber = calendar.get(Calendar.YEAR);
        showDaysChooserCalendar(chatId, messageId, actionItemKey, yearNumber, monthNumber);
    }

    private void showDaysChooserCalendar(Long chatId, int messageId, String actionItemKey, int yearNumber, int monthNumber) {
        Month month = Month.of(monthNumber)/*.plus(1)*/;
        Year year = Year.of(yearNumber);

        String text = "Choose day of <b>" + year + "</b> " + month.name() + ": ";
        InlineKeyboardMarkup replyMarkupControls = getReplyMarkupControls(yearNumber, monthNumber, actionItemKey);

        BaseRequest<?, ?> request;
        if (messageId == -1L) {
            request = new SendMessage(chatId, text)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .disableNotification(false)
                    .replyMarkup(replyMarkupControls);
        } else {
            request = new EditMessageText(chatId, messageId, text)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(false)
                    .replyMarkup(replyMarkupControls);
        }
        bot.execute(request);
    }

    //todo: bug with february month on 2019 year
    private InlineKeyboardMarkup getReplyMarkupControls(int year, int monthNumber, String actionItemKey) {
        Calendar calendar = Calendar.getInstance();
        int actualYear = calendar.get(Calendar.YEAR);
        int actualMonth = calendar.get(Calendar.MONTH) + 1;
        List<List<Integer>> daysOfMonth = getDaysOfMonth(monthNumber, year);
        int numberOfWeeksInMonth = daysOfMonth.size() - 1;
        InlineKeyboardButton[][] inlineKeyboardButtons = new InlineKeyboardButton[numberOfWeeksInMonth + 3][];
        String dateParams = "__" + year + "_" + monthNumber + "__" + actionItemKey;
        if (year > actualYear) {
            inlineKeyboardButtons[0] = new InlineKeyboardButton[2];
            inlineKeyboardButtons[0][1] = new InlineKeyboardButton("Next Year").callbackData("aiDl_choose_next_year" + dateParams);
            inlineKeyboardButtons[0][0] = new InlineKeyboardButton("Previous Year").callbackData("aiDl_choose_previous_year" + dateParams);
        } else {
            inlineKeyboardButtons[0] = new InlineKeyboardButton[1];
            inlineKeyboardButtons[0][0] = new InlineKeyboardButton("Next Year").callbackData("aiDl_choose_next_year" + dateParams);
        }
        for (int i = 0; i < daysOfMonth.size(); i++) {
            List<Integer> daysOfWeek = daysOfMonth.get(i);
            inlineKeyboardButtons[i + 1] = new InlineKeyboardButton[daysOfWeek.size()];
            for (int j = 0; j < daysOfWeek.size(); j++) {
                boolean isNotActualMonth = daysOfWeek.get(j) == -1;
                String text = daysOfWeek.get(j) + "";
                String callbackData = "choose_aiDl__" + year + "__" + daysOfWeek.get(j) + "__" + actionItemKey;
                if(isNotActualMonth) {
                    text = "—";
                    if (i == 0) {
                        callbackData = "aiDl_choose_next_month" + dateParams;
                    } else {
                        callbackData = "aiDl_choose_previous_month" + dateParams;
                    }
                }
                inlineKeyboardButtons[i + 1][j] = new InlineKeyboardButton(text).callbackData(callbackData);
            }
        }
        if ((monthNumber > actualMonth && year == actualYear) || year > actualYear) {
            inlineKeyboardButtons[numberOfWeeksInMonth + 2] = new InlineKeyboardButton[2];
            inlineKeyboardButtons[numberOfWeeksInMonth + 2][1] = new InlineKeyboardButton("Next Month").callbackData("aiDl_choose_next_month" + dateParams);
            inlineKeyboardButtons[numberOfWeeksInMonth + 2][0] = new InlineKeyboardButton("Previous Month").callbackData("aiDl_choose_previous_month" + dateParams);
        } else {
            inlineKeyboardButtons[numberOfWeeksInMonth + 2] = new InlineKeyboardButton[1];
            inlineKeyboardButtons[numberOfWeeksInMonth + 2][0] = new InlineKeyboardButton("Next Month").callbackData("aiDl_choose_next_month" + dateParams);
        }
        return new InlineKeyboardMarkup(inlineKeyboardButtons);
    }

    static List<List<Integer>> getDaysOfMonth(int monthNumber, int yearNumber) {
        List<List<Integer>> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.YEAR, yearNumber);
        calendar.set(Calendar.MONTH, monthNumber - 1);
        int numberOfWeeksInMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        //need to update calendar
        calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        for (int i = 1; i < numberOfWeeksInMonth + 1; i++) {
            int numberOfDaysInWeek = 7;
            ArrayList<Integer> weekDays = new ArrayList<>();
            for (int j = 0; j < numberOfDaysInWeek; j++) {
                if (calendar.get(Calendar.MONTH) == monthNumber - 1) {
                    weekDays.add(calendar.get(Calendar.DAY_OF_MONTH));
                } else {
                    weekDays.add(-1);
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            result.add(weekDays);
        }
        return result;
    }

    private InlineKeyboardMarkup getReplyMarkupControls(Enum[] values, String actionItemKey) {
        InlineKeyboardButton[][] inlineKeyboardButtons = new InlineKeyboardButton[values.length][];
        for (Enum month : values) {
            inlineKeyboardButtons[month.ordinal()] = new InlineKeyboardButton[]{
                    new InlineKeyboardButton(month.name()).callbackData("choose_aiDl__" + month.getDeclaringClass().getSimpleName() + "_" + (month.ordinal()) + "_" + actionItemKey)
            };
        }
        return new InlineKeyboardMarkup(inlineKeyboardButtons);
    }
}

enum HourOfDay {
    eight(8),
    nine(9),
    ten(10),
    eleven(11),
    twelve(12);

    public final int time;

    HourOfDay(int time) {
        this.time = time;
    }
}

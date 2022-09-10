package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import helper.file.SharedObject;
import helper.string.StringHelper;
import telegram.bot.data.Common;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.MessageHelper;

import java.util.*;
import java.util.function.Function;

public class SlotMachineRule implements Rule {
    private TelegramBot bot;
    private List<String> reelTemplate1 = new ArrayList<>(Arrays.asList("🐶", "🐱", "🐭", "🐹", "🐰"));
    private List<String> reelTemplate2 = new ArrayList<>(Arrays.asList("🦋", "🐛", "🐌", "🐚", "🐞", "🐜", "🕷"));
    private List<String> reelTemplate3 = new ArrayList<>(Arrays.asList("🌎", "🌍", "🌏", "🌕", "🌖", "🌗", "🌘", "🌑", "🌒", "🌓", "🌔"));
    private List<String> reelTemplate4 = new ArrayList<>(Arrays.asList("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈", "🍒", "🍑", "🍍", "🥝", "🥑", "🍅", "🍆", "🥒", "🥕", "🌽", "🌶", "🥔", "🍠", "🌰", "🥜", "🍯", "🥐", "🍞", "🥖", "🧀", "🥚", "🍳", "🥓", "🥞", "🍤"));
    private Map<String, Double> symbolsMultiplier;
    private List<String> wildSymbols = new ArrayList<>(Arrays.asList("🐰", "🦋", "🌑", "🍅"));
    private static final String savedUserBalanceKey = "/tmp/slots/savedUsersBalanceV1.ser";
    private static final String savedUserMachineKey = "/tmp/slots/savedUsersMachineV1.ser";
    private final HashMap<Long, Double> savedUsersBalance;
    private final HashMap<Long, SlotMachineData> savedUsersMachine;
    private List<List<String>> reels = new ArrayList<>();
    private List<String> reelNames = new ArrayList<>();
    protected final Map<Long, SlotMachine> slotMachines;
    private Double defaultBalance = 1000D;
    private List<List<Integer>> lines = Arrays.asList(
        new ArrayList<>(Arrays.asList(1 - 1, 1 - 1, 1 - 1, 1 - 1)),
        new ArrayList<>(Arrays.asList(2 - 1, 2 - 1, 2 - 1, 2 - 1)),
        new ArrayList<>(Arrays.asList(3 - 1, 3 - 1, 3 - 1, 3 - 1)),
        new ArrayList<>(Arrays.asList(3 - 1, 2 - 1, 2 - 1, 3 - 1)),
        new ArrayList<>(Arrays.asList(1 - 1, 2 - 1, 2 - 1, 1 - 1)),
        new ArrayList<>(Arrays.asList(1 - 1, 2 - 1, 2 - 1, 3 - 1)),
        new ArrayList<>(Arrays.asList(3 - 1, 2 - 1, 2 - 1, 1 - 1))
    );

    public SlotMachineRule(TelegramBot bot) {
        symbolsMultiplier = new HashMap<>();
        symbolsMultiplier.put("🐶", 1.5);
        symbolsMultiplier.put("🐱", 2d);
        symbolsMultiplier.put("🐭", 3d);
        symbolsMultiplier.put("🐹", 2d);

        symbolsMultiplier.put("🦋", 2d);
        symbolsMultiplier.put("🐛", 4d);
        symbolsMultiplier.put("🐌", 3d);

        symbolsMultiplier.put("🌎", 2d);
        symbolsMultiplier.put("🌍", 4d);
        symbolsMultiplier.put("🌏", 6d);

        symbolsMultiplier.put("🍏", 1.5d);
        symbolsMultiplier.put("🍎", 2d);
        symbolsMultiplier.put("🍐", 3d);
        symbolsMultiplier.put("🍊", 4d);
        symbolsMultiplier.put("🍋", 5d);
        symbolsMultiplier.put("🍌", 6d);

        this.bot = bot;
        reels.add(reelTemplate1);
        reels.add(reelTemplate2);
        reels.add(reelTemplate3);
        reels.add(reelTemplate4);
        reelNames.add("Pigy Wigy");
        reelNames.add("Bugs Classic");
        reelNames.add("Moon wild");
        reelNames.add("Juicy Fruits");
        savedUsersBalance = SharedObject.loadMap(savedUserBalanceKey, new HashMap<>());
        savedUsersMachine = SharedObject.loadMap(savedUserMachineKey, new HashMap<>());
        slotMachines = new HashMap<>();
    }

    @Override
    public boolean guard(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        if (message == null) {
            return false;
        }
        boolean isBot = message.from() != null && message.from().isBot();
        boolean textIsPresent = message.text() != null;
        return !isBot && textIsPresent;
    }

    @Override
    public void run(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        String text = message.text();

        Long chatId = message.chat().id();
        if (Common.data.isGeneralChat(chatId)) {
            return;
        }
        if (text.toLowerCase().contains("спин") || text.toLowerCase().contains("spin")) {
            playTheGame(message, message.from().id(), message.from().firstName());
        }
    }

    public void playTheGame(Message message, Long userId, String who) {
        playTheGame(message, userId, new Random().nextInt(reels.size()), who);
    }

    public void playTheGame(Message message, Long userId, int index, String who) {
        SlotMachine slotMachine = getSlotMachine(userId, index, who);
        slotMachine.run(message);
    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;
        if (isDataPresent) {
            Message message = callbackQuery.message();

            Long chatId = message.chat().id();
            String data = callbackQuery.data();
            if (data.contains("SlotMachine:")) {

                Long userId = Long.valueOf(StringHelper.getRegString(data, "SlotMachine:.*userId:(\\d+).*"));
                if (!userId.equals(callbackQuery.from().id())) {
                    BotHelper.alert(bot, callbackQuery.id(), "Это не ваша игра!\nПишите спин и приобретете свою игру");
                    return;
                }
                if (data.matches("SlotMachine:MachineIsActive:.*")) {
                    BotHelper.alert(bot, callbackQuery.id(), "Погодите слегка машина еще вертиться");
                } else if (data.matches("SlotMachine:PlayAnotherGame:.*")) {
                    getSlotMachine(callbackQuery.from()).removeGameView();
                    SendMessage request = new SendMessage(chatId, "Выберите игру: ")
                        .parseMode(ParseMode.Markdown)
                        .disableWebPagePreview(true)
                        .disableNotification(true);
                    InlineKeyboardButton[] keyboardButtons = new InlineKeyboardButton[reelNames.size()];//{
                    for (int i = 0; i < reelNames.size(); i++) {
                        String reelName = reelNames.get(i);
                        keyboardButtons[i] = new InlineKeyboardButton(reelName).callbackData("SlotMachine:PlayGame:" + reelName + ";userId:" + callbackQuery.from().id());
                    }
                    request = request.replyMarkup(new InlineKeyboardMarkup(keyboardButtons));
                    bot.execute(request);
                } else if (data.matches("SlotMachine:showWinAgain:.*")) {
                    getSlotMachine(callbackQuery.from()).showWinAgain();
                } else if (data.matches("SlotMachine:Spin:.*")) {
                    getSlotMachine(callbackQuery.from()).run(message);
                } else if (data.matches("SlotMachine:MoveDown:.*")) {
                    getSlotMachine(callbackQuery.from()).moveDown();
                } else if (data.matches("SlotMachine:PlayGame:.*")) {
                    String game = StringHelper.getRegString(data, "SlotMachine:PlayGame:([\\w ]+).*");
                    int gameIndex = reelNames.indexOf(game);
                    BotHelper.removeMessage(bot, message.chat().id(), message.messageId());
                    slotMachines.remove(userId);
                    savedUsersMachine.remove(userId);
                    playTheGame(message, userId, gameIndex, callbackQuery.from().firstName());
                }

            }
        }

    }

    private SlotMachine getSlotMachine(User user) {
        return getSlotMachine(user.id(), new Random().nextInt(reels.size()), user.firstName());
    }

    private SlotMachine getSlotMachine(Long userId, int index, String who) {
        Double balance = savedUsersBalance.getOrDefault(userId, defaultBalance);
        SlotMachine machine = slotMachines.getOrDefault(userId, new SlotMachine(
            bot,
            getSlotMachineData(userId, index),
            balance,
            1.5,
            userId,
            symbolsMultiplier,
            wildSymbols,
            who
        ));
        Function<Double, Void> updateUserStats = newBalance -> {
            savedUsersBalance.put(userId, newBalance);
            SharedObject.save(savedUserBalanceKey, savedUsersBalance);
            SharedObject.save(savedUserMachineKey, savedUsersMachine);
            return null;
        };
        slotMachines.put(userId, machine);
        machine.onBalanceChanges(updateUserStats);
        savedUsersBalance.put(userId, balance);
        return machine;
    }

    private SlotMachineData getSlotMachineData(Long userId, int index) {
        SlotMachineData machineData = savedUsersMachine.getOrDefault(userId, new SlotMachineData(
            reelNames.get(index),
            Arrays.asList(
                new ArrayList<>(reels.get(index)),
                new ArrayList<>(reels.get(index)),
                new ArrayList<>(reels.get(index)),
                new ArrayList<>(reels.get(index))
            ),
            lines
        ));
        savedUsersMachine.put(userId, machineData);
        return machineData;
    }

}

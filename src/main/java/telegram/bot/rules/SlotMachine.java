package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.logger.ConsoleLogger;
import telegram.bot.helper.BotHelper;
import telegram.bot.helper.MessageHelper;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SlotMachine implements Rule {
    private final SlotMachineData slotMachineData;
    private TelegramBot bot;
    private final List<List<Integer>> lines;
    private List<String> reel1;
    private List<String> reel2;
    private List<String> reel3;
    private List<String> reel4;
    private static boolean isActive = false;
    private Double balance;
    private Double actualWin = 0d;

    private Double bet;
    private Integer headerMessageId;
    private Integer reelsMessageId;
    private Integer controlsMessageId;
    protected Long chatId;
    private final Long userId;
    private final Map<String, Double> symbolsMultiplier;
    private final List<String> wildSymbols;
    private final String who;

    public SlotMachine(TelegramBot bot, SlotMachineData slotMachineData, Double balance, Double bet, Long userId, Map<String, Double> symbolsMultiplier, List<String> wildSymbols, String who) {
        this.slotMachineData = slotMachineData;
        this.bot = bot;
        this.balance = balance;
        this.lines = slotMachineData.getLines();
        reel1 = slotMachineData.getStrips().get(0);
        reel2 = slotMachineData.getStrips().get(1);
        reel3 = slotMachineData.getStrips().get(2);
        reel4 = slotMachineData.getStrips().get(3);
        this.bet = bet;
        this.userId = userId;
        this.symbolsMultiplier = symbolsMultiplier;
        this.wildSymbols = wildSymbols;
        this.who = who;
    }

    @Override
    public void run(Update update) {
        Message message = MessageHelper.getAnyMessage(update);
        run(message);
    }

    public void run(Message message) {
        chatId = message.chat().id();
        if (isActive) {
            sendMessage(chatId, "Простите, слот машина пока занята");
            return;
        }
        updateGameView();
        isActive = true;
        actualWin = 0d;
        int mulitplier = 1;
        if (message.chat().type() == Chat.Type.Private) {
            mulitplier = 2;
        }
        int finalMulitplier = mulitplier;
        balance -= bet;
        updateBalance();
        new Thread(() -> {
            int timeout = 10 * finalMulitplier;
            shuffle();
            while (timeout > 0) {
                if (timeout > 8 * finalMulitplier) {
                    roll(reel1);
                }
                if (timeout > 4 * finalMulitplier) {
                    roll(reel2);
                }
                if (timeout > 2 * finalMulitplier) {
                    roll(reel3);
                }
                roll(reel4);
                sleep(1000 / (10 * finalMulitplier), TimeUnit.MILLISECONDS);
                String messageTxt = getSlotsAsString();
                editMessage(chatId, reelsMessageId, messageTxt);
                timeout--;
            }
            showWinIfPresent();
            isActive = false;
            updateBalance();
        }).start();
    }

    public void updateGameView() {
        if (reelsMessageId == null) {
            headerMessageId = sendMessage(chatId, getSlotsHeader());
            reelsMessageId = sendMessage(chatId, getSlotsAsString());
            updateControls();
        } else {
            editMessage(chatId, reelsMessageId, getSlotsAsString());
        }
    }

    public void showWinAgain() {
        balance -= actualWin;
        actualWin = 0d;
        showWinIfPresent();
    }

    public void moveDown() {
        removeGameView();
        updateGameView();
    }

    public void removeGameView() {
        BotHelper.removeMessage(bot, chatId, headerMessageId);
        BotHelper.removeMessage(bot, chatId, reelsMessageId);
        BotHelper.removeMessage(bot, chatId, controlsMessageId);

        headerMessageId = null;
        reelsMessageId = null;
        controlsMessageId = null;
    }

    private void showWinIfPresent() {
        double lineBet = bet / lines.size();
        Double win = 0d;
        int lineId = 0;
        for (List<Integer> line : lines) {
            Double lineWin = 0d;
            int symbolsCount = 0;
            String lineSymbol = getLineSymbolWhichNotAWild(line);
            for (int i = 1; i < line.size(); i++) {
                boolean isWinSymbol = lineSymbol.equals(slotMachineData.getStrips().get(i).get(line.get(i)));
                boolean isWildSymbol = wildSymbols.contains(slotMachineData.getStrips().get(i).get(line.get(i)));
                if (isWinSymbol || isWildSymbol) {
                    lineWin = lineBet * i * symbolsMultiplier.getOrDefault(lineSymbol, 1d);
                    symbolsCount = i;
                } else {
                    break;
                }
            }
            if (symbolsCount >= 2 && lineWin > 0) {
                balance += lineWin;
                win += lineWin;
                String messageTxt1 = getSlotsWithoutWinLineAsString(lineId, symbolsCount);
                String messageTxt = getSlotsAsString();
                editMessage(chatId, reelsMessageId, messageTxt1);
                updateBalance();
                sleep(200, TimeUnit.MILLISECONDS);
                editMessage(chatId, reelsMessageId, messageTxt);
                sleep(200, TimeUnit.MILLISECONDS);
                editMessage(chatId, reelsMessageId, messageTxt1);
                sleep(200, TimeUnit.MILLISECONDS);
                editMessage(chatId, reelsMessageId, messageTxt);
            }
            actualWin = win;
            lineId++;
        }

    }

    private String getLineSymbolWhichNotAWild(List<Integer> line) {
        int index = 0;
        List<String> strip = slotMachineData.getStrips().get(index);
        for (Integer offset : line) {
            String result = strip.get(offset);
            if (!wildSymbols.contains(result)) {
                return result;
            } else {
                strip = slotMachineData.getStrips().get(++index);
            }

        }

        return strip.get(line.get(0));
    }

    private void runWinMeter() {

    }

    private void updateBalance() {
        if (onBalanceChangedListener != null) {
            updateControls();
            onBalanceChangedListener.apply(balance);
        }
    }

    private void updateControls() {
        String messageTxt = getSlotsControls();
        if (controlsMessageId == null) {
            controlsMessageId = sendMessage(chatId, messageTxt);
        }
        if (isActive) {
            editMessage(chatId, controlsMessageId, messageTxt, new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Не активно").callbackData("SlotMachine:MachineIsActive:userId:" + userId),
                new InlineKeyboardButton("Не активно").callbackData("SlotMachine:MachineIsActive:userId:" + userId),
                new InlineKeyboardButton("Не активно").callbackData("SlotMachine:MachineIsActive:userId:" + userId),
                new InlineKeyboardButton("Не активно").callbackData("SlotMachine:MachineIsActive:userId:" + userId),
            }));
        } else {
            editMessage(chatId, controlsMessageId, messageTxt, new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Другую игру").callbackData("SlotMachine:PlayAnotherGame:userId:" + userId),
                new InlineKeyboardButton("🎰").callbackData("SlotMachine:Spin:userId:" + userId),
                new InlineKeyboardButton("🦵👇").callbackData("SlotMachine:MoveDown:userId:" + userId),
                new InlineKeyboardButton("??").callbackData("SlotMachine:showWinAgain:userId:" + userId),
            }));
        }
    }

    private Function<Double, Void> onBalanceChangedListener;

    public void onBalanceChanges(Function<Double, Void> function) {
        onBalanceChangedListener = function;

    }

    private void shuffle() {
        shuffleReel(reel1);
        shuffleReel(reel2);
        shuffleReel(reel3);
        shuffleReel(reel4);
    }

    private void shuffleReel(List<String> reel) {
        for (int i = 0; i < new Random().nextInt(8); i++) {
            roll(reel);
        }
    }

    private String getSlotsHeader() {
        return String.format("%s%n", slotMachineData.getMachineName());
    }

    private String getSlotsWithoutWinLineAsString(Integer lineId, Integer symbolsCount) {
        List<Integer> line = slotMachineData.lines.get(lineId);
        StringBuilder result = new StringBuilder();
        String winSymbol = "🎰";
        for (int i = 0; i < 3; i++) {
            result.append((symbolsCount >= 0 && line.get(0) == i) ? winSymbol : reel1.get(i));
            result.append((symbolsCount >= 1 && line.get(1) == i) ? winSymbol : reel2.get(i));
            result.append((symbolsCount >= 2 && line.get(2) == i) ? winSymbol : reel3.get(i));
            result.append((symbolsCount >= 3 && line.get(3) == i) ? winSymbol : reel4.get(i));
            result.append("\n");
        }
        return result.toString();
    }

    private String getSlotsAsString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            result.append(reel1.get(i));
            result.append(reel2.get(i));
            result.append(reel3.get(i));
            result.append(reel4.get(i));
            result.append("\n");
        }
        return result.toString();
    }

    private String getSlotsControls() {
        return String.format(
            "Ставка: ** %.2f **%nКредит: ** %.2f **%nВыигрыш: ** %.2f **%nИграет: ** %s **",
            bet, balance, actualWin, who
        );
    }

    private void sleep(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            ConsoleLogger.logErrorFor(this, e);
        }
    }

    private Integer sendMessage(Long chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    private Integer sendMessage(Long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(true)
            .disableNotification(true);
        if (inlineKeyboardMarkup != null) {
            request = request.replyMarkup(inlineKeyboardMarkup);
        }
        SendResponse execute = bot.execute(request);
        return execute.message().messageId();
    }

    private void editMessage(Long chatId, int messageId, String text) {
        editMessage(chatId, messageId, text, null);
    }

    private void editMessage(Long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            EditMessageText request = new EditMessageText(chatId, messageId, text)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true);
            if (inlineKeyboardMarkup != null) {
                request = request.replyMarkup(inlineKeyboardMarkup);
            }
            bot.execute(request);
        } catch (RuntimeException e) {
            ConsoleLogger.logErrorFor(this, e);
            sleep(1, TimeUnit.SECONDS);
        }

    }

    private void roll(List<String> reel) {
        int size = reel.size();
        String remove = reel.remove(size - 1);
        reel.add(0, remove);
    }


    public Double getBet() {
        return bet;
    }

    public void setBet(Double bet) {
        this.bet = bet;
    }
}

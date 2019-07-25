package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import helper.logger.ConsoleLogger;
import telegram.bot.data.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SlotMachine implements Rule {
    private TelegramBot bot;
    private List<String> reelTemplate;// = Arrays.asList("🐶", "🐱", "🐭", "🐹", "🐰");
    private List<String> reel1;
    private List<String> reel2;
    private List<String> reel3;
    private List<String> reel4;
    private List<String> reel5;
    private static boolean isActive = true;

    public SlotMachine(TelegramBot bot, List<String> strings) {
        this.bot = bot;
        reelTemplate = strings;
        reel1 = initReel();
        reel2 = initReel();
        reel3 = initReel();
        reel4 = initReel();
        reel5 = initReel();
    }

    private List<String> initReel() {
        List<String> reel = new ArrayList<>();
        reel.addAll(reelTemplate);
        return reel;

    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        Long chatId = message.chat().id();
        if (Common.data.isGeneralChat(chatId)) {
            return;
        }
        if (message.text().contains("спин")) {
            if (!isActive){
                sendMessage(chatId, "Простите, слот машина пока занята");
                return;
            }
            Integer messageId = sendMessage(chatId, getSlotsAsString());
            isActive= false;
            int mulitplier = 1;
            if(message.chat().type() == Chat.Type.Private){
                mulitplier = 2;
            }
            int finalMulitplier = mulitplier;
            new Thread(() -> {
                int timeout = 10 * finalMulitplier;
                shuffle();
                while (timeout > 0) {
                    if (timeout > 8*finalMulitplier) {
                        roll(reel1);
                    }
                    if (timeout > 4*finalMulitplier) {
                        roll(reel2);
                    }
                    if (timeout > 2*finalMulitplier) {
                        roll(reel3);
                    }
                    roll(reel4);
                    sleep(1000/(10*finalMulitplier), TimeUnit.MILLISECONDS);
                    String messageTxt = getSlotsAsString();
                    editMessage(chatId, messageId, messageTxt);
                    timeout--;
                }
                isActive = true;
            }).start();
        }
    }

    private void shuffle() {
        shuffleReel(reel1);
        shuffleReel(reel2);
        shuffleReel(reel3);
        shuffleReel(reel4);
//        shuffleReel(reel5);
    }

    private void shuffleReel(List<String> reel) {
        for (int i = 0; i < new Random().nextInt(8); i++) {
            roll(reel);
        }
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

    private void sleep(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            ConsoleLogger.logErrorFor(this, e);
        }
    }

    private Integer sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(true)
            .disableNotification(true);
        SendResponse execute = bot.execute(request);
        return execute.message().messageId();
    }

    private void editMessage(Long chatId, int messageId, String text) {
        try {
            EditMessageText request = new EditMessageText(chatId, messageId, text)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true);
            bot.execute(request);
        } catch (RuntimeException e){
            ConsoleLogger.logErrorFor(this, e);
            sleep(1, TimeUnit.SECONDS);
        }

    }

    private void roll(List<String> reel) {
        int size = reel.size();
        String remove = reel.remove(size-1);
        reel.add(0, remove);
    }
}

package telegram.bot.rules;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SlotMachineRule implements Rule {
    private TelegramBot bot;
    private List<String> reelTemplate1 = Arrays.asList("🐶", "🐱", "🐭", "🐹", "🐰");
    private List<String> reelTemplate2 = Arrays.asList("🦋","🐛","🐌","🐚","🐞","🐜","🕷");
    private List<String> reelTemplate3 = Arrays.asList("🌎","🌍","🌏","🌕","🌖","🌗","🌘","🌑","🌒","🌓","🌔");
    private List<String> reelTemplate4 = Arrays.asList("🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🍈","🍒","🍑","🍍","🥝","🥑","🍅","🍆","🥒","🥕","🌽","🌶","🥔","🍠","🌰","🥜","🍯","🥐","🍞","🥖","🧀","🥚","🍳","🥓","🥞","🍤");
//    private List<String> reelTemplate4 = Arrays.asList("0","1","2","3","4","5","6","7","8","9");
    private List<List<String>> reels = new ArrayList<>();

    public SlotMachineRule(TelegramBot bot) {
        this.bot = bot;
        reels.add(reelTemplate1);
        reels.add(reelTemplate2);
        reels.add(reelTemplate3);
        reels.add(reelTemplate4);
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text();
        if(text == null){
            return;
        }
        if (text.toLowerCase().contains("спин")|| text.toLowerCase().contains("spin")) {
            new SlotMachine(bot, reels.get(new Random().nextInt(reels.size()))).run(update);
        }
    }

}

package telegram.bot.rules.world;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import telegram.bot.rules.Rule;

import java.awt.*;
import java.util.*;
import java.util.List;

public class WorldGameRule implements Rule, World {
    private TelegramBot bot;
    private final List<List<WorldEntity>> worldList = new ArrayList<>();
    private int height;
    private int width;
    private final Map<WoldControl, List<WorldEntity>> worldControlListeners = new HashMap<>();
    private final Map<Long, Integer> chatIdOnMessageIdMap = new HashMap<>();

    public WorldGameRule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void callback(CallbackQuery callbackQuery) {
        boolean isDataPresent = callbackQuery.from() != null && callbackQuery.data() != null;
        boolean isUpdated = false;
        if (isDataPresent) {
            String data = callbackQuery.data();
            WoldControl woldControl = WoldControl.getControlByCommand(data);
            if (worldControlListeners.containsKey(woldControl)) {
                for (WorldEntity worldEntity : worldControlListeners.get(woldControl)) {
                    worldEntity.handle(woldControl);
                    handleWorldEntityOnMoving(worldEntity);
                    isUpdated = true;
                }
            }
        }
        if (isUpdated) {
            updateMessage(callbackQuery.message().chat().id());
        }
    }

    private void handleWorldEntityOnMoving(WorldEntity worldEntity) {
        if (worldEntity instanceof Movable) {
            Movable movable = (Movable) worldEntity;
            Point entityPosition = getEntityPosition(worldEntity);
            if (entityPosition != null) {
                int newX = entityPosition.x + movable.getXOffset();
                int newY = entityPosition.y + movable.getYOffset();
                if (newX < 0) {
                    newX = this.height - 1;
                }
                if (newX >= this.height) {
                    newX = 0;
                }
                if (newY < 0) {
                    newY = this.width - 1;
                }
                if (newY >= this.width) {
                    newY = 0;
                }
                WorldEntity worldEntityFromNewLocation = worldList.get(newX).get(newY);
                addEntity(worldEntity, newX, newY);
                worldEntityFromNewLocation.removed();
                addEntity(new EmptyWorldEntity(), entityPosition.x, entityPosition.y);
                movable.moved(worldEntityFromNewLocation, entityPosition);
            }
        }
    }

    @Override
    public Point getEntityPosition(WorldEntity worldEntity) {
        for (int x = 0; x < worldList.size(); x++) {
            for (int y = 0; y < worldList.get(x).size(); y++) {
                if (worldList.get(x).get(y).equals(worldEntity)) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    @Override
    public void run(Update update) {
        Message message = update.message() == null ? update.editedMessage() : update.message();
        String text = message.text();
        if (text == null) {
            return;
        }
        text = text.toLowerCase();
        if (text.contains("stopTheGame".toLowerCase())) {
            chatIdOnMessageIdMap.remove(message.chat().id());
//            worldControlListeners.clear();
        }
        if (text.contains("runTheGame".toLowerCase())) {
            Integer messageId = sendMessage(message.chat().id(), getMessage()).message().messageId();
            chatIdOnMessageIdMap.put(message.chat().id(), messageId);
        }
    }

    private String getMessage() {
        StringBuilder result = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String symbols = worldList.get(x).get(y).getSymbols();
                result.append(symbols);
                if (x == width - 1) {
                    result.append("\n");
                }
            }
        }
        return result.toString();
    }

    private SendResponse sendMessage(long chatId, String message) {
        SendMessage request = new SendMessage(chatId, "Like it: \n" + message)
            .parseMode(ParseMode.HTML)
            .disableWebPagePreview(false)
            .disableNotification(false)
            .replyMarkup(getReplyMarkupControls());
        return bot.execute(request);
    }

    private void updateMessage(Long chatId) {
        if (chatIdOnMessageIdMap.containsKey(chatId)) {
            Integer messageId = chatIdOnMessageIdMap.get(chatId);
            updateMessage(chatId, messageId);
        }
    }

    private void updateMessage(Long chatId, Integer messageId) {
        try {
            EditMessageText request = new EditMessageText(chatId, messageId, "Like it: \n" + getMessage())
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .replyMarkup(getReplyMarkupControls());
            bot.execute(request);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getReplyMarkupControls() {
        return new InlineKeyboardMarkup(new InlineKeyboardButton[] {
            new InlineKeyboardButton("🔼").callbackData("go_top")
        }, new InlineKeyboardButton[] {
            new InlineKeyboardButton("◀️").callbackData("go_left"),
            new InlineKeyboardButton("▶️").callbackData("go_right")
        }, new InlineKeyboardButton[] {
            new InlineKeyboardButton("🔽").callbackData("go_bottom")
        });
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        initializeCells();
    }

    private void initializeCells() {
        worldList.clear();
        for (int x = 0; x < width; x++) {
            ArrayList<WorldEntity> columnList = new ArrayList<>();
            worldList.add(columnList);
            for (int y = 0; y < height; y++) {
                columnList.add(new EmptyWorldEntity().setWorld(this));
            }
        }
    }

    @Override
    public void addEntity(WorldEntity worldEntity, int x, int y) {
        worldList.get(x).set(y, worldEntity.setWorld(this));
    }

    @Override
    public void addEntity(WorldEntity worldEntity, int x, int y, WoldControl[] attachWoldControls) {
        addEntity(worldEntity, x, y);
        if (attachWoldControls != null) {
            attachEntityToControls(worldEntity, attachWoldControls);
        }
    }

    private void attachEntityToControls(WorldEntity worldEntity, WoldControl[] attachWoldControls) {
        for (WoldControl woldControl : attachWoldControls) {
            if (!worldControlListeners.containsKey(woldControl)) {
                worldControlListeners.put(woldControl, new ArrayList<>());
            }
            worldControlListeners.get(woldControl).add(worldEntity);
        }
    }
}

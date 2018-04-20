package telegram.bot.rules.world;

import java.awt.*;

public interface World {
    Point getEntityPosition(WorldEntity worldEntity);

    void addEntity(WorldEntity worldEntity, int rows, int columns);

    void addEntity(WorldEntity worldEntity, int rows, int columns, WoldControl[] attachWoldControls);
}

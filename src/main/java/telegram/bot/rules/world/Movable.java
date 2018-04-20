package telegram.bot.rules.world;

import java.awt.*;

public interface Movable {
    void moved(WorldEntity worldEntity, Point from);
    default int getXOffset() {
        return 0;
    }
    default int getYOffset() {
        return 0;
    }
}

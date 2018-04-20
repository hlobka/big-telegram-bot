package telegram.bot.rules.world;

import java.awt.*;

public class Rabbit implements WorldEntity, Movable {
    private World world;
    protected int xOffset = 0;
    protected int yOffset = 0;
    protected int lifeItter = 0;

    @Override
    public void moved(WorldEntity entity, Point from) {
        xOffset = 0;
        yOffset = 0;
        lifeItter++;
        if(lifeItter %5 == 0){
            world.addEntity(new EmptyWorldEntity("💩"), from.x, from.y);
//            Point entityPosition = world.getEntityPosition(this);
        }
    }

    @Override
    public int getXOffset() {
        return xOffset;
    }

    @Override
    public int getYOffset() {
        return yOffset;
    }

    @Override
    public WorldEntity setWorld(World world) {
        this.world = world;
        return this;
    }

    @Override
    public String getSymbols() {
        return "🐇";
    }

    @Override
    public void handle(WoldControl woldControl) {
        switch (woldControl){
            case BTN_TOP:
                yOffset -= 1;
                break;
            case BTN_BOTTOM:
                yOffset += 1;
                break;
            case BTN_RIGHT:
                xOffset += 1;
                break;
            case BTN_LEFT:
                xOffset -= 1;
                break;
        }
    }
}

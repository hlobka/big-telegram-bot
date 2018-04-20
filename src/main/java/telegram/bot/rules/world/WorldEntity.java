package telegram.bot.rules.world;

public interface WorldEntity {
    WorldEntity setWorld(World world);

    default String getSymbols(){
        return "◼";
    }

    default void handle(WoldControl woldControl){

    }

    default void removed(){

    }
}

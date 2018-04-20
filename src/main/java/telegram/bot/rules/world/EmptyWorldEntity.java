package telegram.bot.rules.world;

public class EmptyWorldEntity implements WorldEntity {
    private World world;
    private String symbol;

    public EmptyWorldEntity() {
        this("◼");
    }


    public EmptyWorldEntity(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getSymbols() {
        return symbol;
    }

    @Override
    public WorldEntity setWorld(World world) {
        this.world = world;
        return this;
    }
}

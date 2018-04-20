package telegram.bot.rules.world;

public enum WoldControl {
    BTN_RIGHT("▶️", "go_right"),
    BTN_LEFT("◀️", "go_left"),
    BTN_TOP("🔼", "go_top"),
    BTN_BOTTOM("🔽", "go_bottom");

    private final String label;
    private final String command;

    WoldControl(String label, String command) {
        this.label = label;
        this.command = command;
    }

    public String getLabel() {
        return label;
    }

    public String getCommand() {
        return command;
    }

    public static WoldControl getControlByCommand(String command){
        for (WoldControl woldControl : values()) {
            if(woldControl.getCommand().equals(command)){
                return woldControl;
            }
        }
        throw new IllegalArgumentException(String.format("command: %s is not WorldControl", command));

    }
}

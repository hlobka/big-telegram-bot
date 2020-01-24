package telegram.bot.checker.statuses;

public enum MemberWorkStatus implements MemberStatus {
    WALKING("🚗"),
    ONLINE("🍏"),
    BUSY("🍎"),
    LUNCH("🍽"),
    SICK("🤢"),
    OFFLINE("🍹"),
    HELP("?");

    private final String status;

    MemberWorkStatus(String status) {

        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String type() {
        return "Work";
    }
}

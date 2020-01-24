package telegram.bot.checker.statuses;

public enum MemberFunStatus implements MemberStatus {
    CRAZY("🤪"),
    HAPPY("😂🤣"),
    LSD("🌈"),
    HELL("🔥"),
    WELL("👍"),
    HELP("?");

    private final String status;

    MemberFunStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String type() {
        return "Fun";
    }
}

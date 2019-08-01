package telegram.bot.checker.workFlow.implementations.services;

import java.util.function.Consumer;

public interface ServiceProvider<T> {
    void provide(Consumer<T> consumer);
    void renew(Consumer<T> consumer);
}

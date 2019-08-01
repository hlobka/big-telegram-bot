package telegram.bot.checker.workFlow.implementations.services;

import telegram.bot.data.Common;
import upsource.UpsourceApi;

import java.util.function.Consumer;

public class UpsourceServiceProvider implements ServiceProvider<UpsourceApi> {
    private UpsourceApi upsourceApi;
    @Override
    public void provide(Consumer<UpsourceApi> consumer) {
        try {
            if (upsourceApi == null) {
                renew(consumer);
            } else {
                consumer.accept(upsourceApi);
            }
        } catch (RuntimeException e) {
            upsourceApi = null;
            renew(consumer);
        }
    }

    @Override
    public void renew(Consumer<UpsourceApi> consumer) {
        upsourceApi = new UpsourceApi(Common.UPSOURCE.url, Common.UPSOURCE.login, Common.UPSOURCE.pass);
        consumer.accept(upsourceApi);
    }
}

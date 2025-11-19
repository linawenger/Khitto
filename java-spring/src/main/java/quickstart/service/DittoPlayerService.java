package quickstart.service;

import com.ditto.java.*;
import com.ditto.java.serialization.DittoCborSerializable;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class DittoPlayerService {

    private static final String PLAYERS_COLLECTION_NAME = "players";
    private final DittoService dittoService;

    public DittoPlayerService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    public void addPlayer(@Nonnull String ip) {
        dittoService.getDitto().getStore().execute(
                "INSERT INTO %s DOCUMENTS (:newPlayer)".formatted(PLAYERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newPlayer",
                                                        DittoCborSerializable.Dictionary.buildDictionary()
                                                                                        .put("_id", UUID.randomUUID().toString())
                                                                                        .put("ip", ip)
                                                                                        .build())
                                                .build()
        ).toCompletableFuture().join();
    }

    public void updatePlayerIp(@Nonnull String pid, @Nonnull String newIp) {
        dittoService.getDitto().getStore().execute(
                "UPDATE %s SET ip = :newIp WHERE _id = :pid".formatted(PLAYERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newIp", newIp)
                                                .put("pid", pid)
                                                .build()
        ).toCompletableFuture().join();
    }

    public void deletePlayer(@Nonnull String pid) {
        dittoService.getDitto().getStore().execute(
                "DELETE FROM %s WHERE _id = :pid".formatted(PLAYERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("pid", pid)
                                                .build()
        ).toCompletableFuture().join();
    }

    @Nonnull
    public Flux<List<Player>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY ip ASC".formatted(PLAYERS_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToPlayer).toList())
                );

                emitter.onDispose(() -> {
                    try {
                        subscription.close();
                        observer.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (DittoError e) {
                emitter.error(e);
            }
        }, FluxSink.OverflowStrategy.LATEST);
    }

    private Player itemToPlayer(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();
        return new Player(
                value.get("_id").getString(),
                value.get("ip").getString()
        );
    }
}

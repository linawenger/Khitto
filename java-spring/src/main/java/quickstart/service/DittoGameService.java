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
public class DittoGameService {
    private static final String GAMES_COLLECTION_NAME = "games";
    private final DittoService dittoService;

    public DittoGameService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    // --- CREATE ---
    public void addGame(@Nonnull String status) {
        dittoService.getDitto().getStore().execute(
                "INSERT INTO %s DOCUMENTS (:newGame)".formatted(GAMES_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newGame",
                                                        DittoCborSerializable.Dictionary.buildDictionary()
                                                                                        .put("_id", UUID.randomUUID().toString())
                                                                                        .put("status", status)
                                                                                        .build())
                                                .build()
        ).toCompletableFuture().join();
    }

    // --- UPDATE ---
    public void updateGameStatus(@Nonnull String gameId, @Nonnull String newStatus) {
        dittoService.getDitto().getStore().execute(
                "UPDATE %s SET status = :newStatus WHERE _id = :gameId".formatted(GAMES_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newStatus", newStatus)
                                                .put("gameId", gameId)
                                                .build()
        ).toCompletableFuture().join();
    }

    // --- DELETE (optional, if you want it) ---
    public void deleteGame(@Nonnull String gameId) {
        dittoService.getDitto().getStore().execute(
                "DELETE FROM %s WHERE _id = :gameId".formatted(GAMES_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("gameId", gameId)
                                                .build()
        ).toCompletableFuture().join();
    }

    // --- READ / OBSERVE ---
    @Nonnull
    public Flux<List<Game>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY status ASC".formatted(GAMES_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToGame).toList())
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

    private Game itemToGame(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();
        return new Game(
                value.get("_id").getString(),
                value.get("status").getString()
        );
    }
}

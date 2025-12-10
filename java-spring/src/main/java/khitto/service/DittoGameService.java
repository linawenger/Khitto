package khitto.service;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ditto.java.Ditto;
import com.ditto.java.DittoError;
import com.ditto.java.DittoQueryResult;
import com.ditto.java.DittoQueryResultItem;
import com.ditto.java.DittoStoreObserver;
import com.ditto.java.DittoSyncSubscription;
import com.ditto.java.serialization.DittoCborSerializable;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Component
public class DittoGameService {

    private static final String GAMES_COLLECTION_NAME = "games";
    private final DittoService dittoService;

    public DittoGameService(DittoService dittoService) {
        this.dittoService = dittoService;

        Ditto ditto = dittoService.getDitto();
        try {
            ditto.getSync().registerSubscription("SELECT * FROM %s".formatted(GAMES_COLLECTION_NAME));
        } catch (DittoError ignored) {}
    }

    public java.util.List<khitto.model.Game> findAll() {
        final String query = "SELECT * FROM %s".formatted(GAMES_COLLECTION_NAME);
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(query)
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(this::itemToModelGame)
                         .filter(g -> !g.isDeleted())
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    private java.util.List<khitto.model.Game> findAllIncludingDeleted() {
        final String query = "SELECT * FROM %s".formatted(GAMES_COLLECTION_NAME);
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(query)
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(this::itemToModelGame)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    public java.util.Optional<khitto.model.Game> findById(int id) {
        final String query = "SELECT * FROM %s WHERE id = :id".formatted(GAMES_COLLECTION_NAME);
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(query,
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("id", String.valueOf(id))
                                                                               .build())
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .findFirst()
                         .map(this::itemToModelGame);
        } finally {
            closeQuietly(result);
        }
    }

    public void save(khitto.model.Game game) {
        Ditto ditto = dittoService.getDitto();

        DittoQueryResult deleteResult = ditto.getStore()
                                             .execute(
                                                     "DELETE FROM %s WHERE id = :id".formatted(GAMES_COLLECTION_NAME),
                                                     DittoCborSerializable.Dictionary.buildDictionary()
                                                                                     .put("id", String.valueOf(game.getId()))
                                                                                     .build()
                                             )
                                             .toCompletableFuture()
                                             .join();
        closeQuietly(deleteResult);

        DittoCborSerializable.Dictionary gameDoc =
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("_id", UUID.randomUUID().toString())
                                                .put("id", String.valueOf(game.getId()))
                                                .put("name", game.getName())
                                                .put("status", String.valueOf(game.getStatus()))
                                                .put("finished", String.valueOf(game.isFinished()))
                                                .put("deleted", String.valueOf(game.isDeleted()))
                                                .build();

        DittoQueryResult insertResult = ditto.getStore()
                                             .execute(
                                                     "INSERT INTO %s DOCUMENTS (:newGame)".formatted(GAMES_COLLECTION_NAME),
                                                     DittoCborSerializable.Dictionary.buildDictionary()
                                                                                     .put("newGame", gameDoc)
                                                                                     .build()
                                             )
                                             .toCompletableFuture()
                                             .join();
        closeQuietly(insertResult);
    }


    public void delete(int id) {
        findById(id).ifPresent(game -> {
            game.setDeleted(true);
            save(game);
        });
    }

    public int getNextId() {
        return findAllIncludingDeleted().stream()
                        .mapToInt(khitto.model.Game::getId)
                        .max()
                        .orElse(0) + 1;
    }

    @Nonnull
    public Flux<java.util.List<khitto.model.Game>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY status ASC".formatted(GAMES_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToModelGame).toList())
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

    private khitto.model.Game itemToModelGame(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();

        String idStr      = value.get("id")       != null ? value.get("id").getString()       : "0";
        String name       = value.get("name")     != null ? value.get("name").getString()     : "";
        String statusStr  = value.get("status")   != null ? value.get("status").getString()   : "0";
        String finishedStr= value.get("finished") != null ? value.get("finished").getString() : "false";
        String deletedStr = value.get("deleted")  != null ? value.get("deleted").getString()  : "false"; // 🔥 neu

        int id = Integer.parseInt(idStr);
        int status = Integer.parseInt(statusStr);
        boolean finished = Boolean.parseBoolean(finishedStr);
        boolean deleted  = Boolean.parseBoolean(deletedStr);

        return new khitto.model.Game(id, name, status, finished, deleted);
    }

    private void closeQuietly(DittoQueryResult result) {
        if (result == null) return;
        try {result.close();}
        catch (IOException ignored) {}
    }
}

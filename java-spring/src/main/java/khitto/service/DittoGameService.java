package khitto.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ditto.java.Ditto;
import com.ditto.java.DittoError;
import com.ditto.java.DittoQueryResult;
import com.ditto.java.serialization.DittoCborSerializable;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DittoGameService {

    private static final String GAMES_COLLECTION_NAME = "games";
    private final DittoService dittoService;
    private final DittoObservationService observationService;

    public DittoGameService(DittoService dittoService,
                            DittoObservationService observationService) {
        this.dittoService = dittoService;
        this.observationService = observationService;

        Ditto ditto = dittoService.getDitto();
        try {
            ditto.getSync()
                 .registerSubscription("SELECT * FROM %s".formatted(GAMES_COLLECTION_NAME));
        } catch (DittoError ignored) {
        }
    }

    public List<khitto.model.Game> findAll() {
        return loadAllGamesRaw().stream()
                                .filter(g -> !g.isDeleted())
                                .collect(Collectors.toList());
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
                         .map(ItemToModel::game);
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
        return loadAllGamesRaw().stream()
                                .mapToInt(khitto.model.Game::getId)
                                .max()
                                .orElse(0) + 1;
    }

    private List<khitto.model.Game> loadAllGamesRaw() {
        final String query = "SELECT * FROM %s".formatted(GAMES_COLLECTION_NAME);
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(query)
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(ItemToModel::game)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    private void closeQuietly(DittoQueryResult result) {
        if (result == null) return;
        try {result.close();}
        catch (IOException ignored) {}
    }

    @Nonnull
    public Flux<List<khitto.model.Game>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY status ASC".formatted(GAMES_COLLECTION_NAME);
        return observationService.observeList(query, ItemToModel::game);
    }
}

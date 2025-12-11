package khitto.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ditto.java.Ditto;
import com.ditto.java.DittoQueryResult;
import com.ditto.java.serialization.DittoCborSerializable;
import khitto.model.Game;
import org.springframework.stereotype.Component;

@Component
public class DittoGameService {

    private static final String GAMES_COLLECTION_NAME = "games";

    private final DittoService dittoService;

    public DittoGameService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    public List<Game> findAll() {
        return loadAllGamesRaw().stream()
                                .filter(g -> !g.isDeleted())
                                .toList();
    }

    public Optional<Game> findById(int id) {
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

    public void save(Game game) {
        Ditto ditto = dittoService.getDitto();

        // Alte Version (falls vorhanden) löschen
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
                                .mapToInt(Game::getId)
                                .max()
                                .orElse(0) + 1;
    }

    private List<Game> loadAllGamesRaw() {
        final String query = "SELECT * FROM %s".formatted(GAMES_COLLECTION_NAME);
        Ditto ditto = dittoService.getDitto();

        DittoQueryResult result = ditto.getStore()
                                       .execute(query)
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(ItemToModel::game)
                         .toList();
        } finally {
            closeQuietly(result);
        }
    }

    private void closeQuietly(DittoQueryResult result) {
        if (result == null) return;
        try {
            result.close();
        } catch (IOException ignored) {
        }
    }
}

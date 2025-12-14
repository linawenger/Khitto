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
public class DittoQuestionService {

    private static final String QUESTIONS_COLLECTION_NAME = "questions";
    private final DittoService dittoService;
    private final DittoObservationService observationService;

    public DittoQuestionService(DittoService dittoService,
                                DittoObservationService observationService) {
        this.dittoService = dittoService;
        this.observationService = observationService;

        Ditto ditto = dittoService.getDitto();
        try {
            ditto.getSync().registerSubscription("SELECT * FROM %s".formatted(QUESTIONS_COLLECTION_NAME));
        } catch (DittoError ignored) {}
    }

    public List<khitto.model.Question> findByGameId(int gameId) {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "SELECT * FROM %s WHERE gameId = :gameId".formatted(QUESTIONS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("gameId", String.valueOf(gameId))
                                                                               .build())
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(ItemToModel::question)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    public int getNextOddId() {
        List<khitto.model.Question> all = findAllInternal();
        int max = all.stream().mapToInt(khitto.model.Question::getId).max().orElse(0);

        int next = max < 1 ? 1 : max + 2;
        return next % 2 == 1 ? next : next + 1;
    }

    public void saveAll(List<khitto.model.Question> questions) {
        Ditto ditto = dittoService.getDitto();

        for (khitto.model.Question q : questions) {
            DittoQueryResult result = ditto.getStore()
                                           .execute(
                                                   "INSERT INTO %s DOCUMENTS (:newQuestion)".formatted(QUESTIONS_COLLECTION_NAME),
                                                   DittoCborSerializable.Dictionary.buildDictionary()
                                                                                   .put("newQuestion",
                                                                                           DittoCborSerializable.Dictionary.buildDictionary()
                                                                                                                           .put("_id", UUID.randomUUID().toString())
                                                                                                                           .put("id", String.valueOf(q.getId()))
                                                                                                                           .put("gameId", String.valueOf(q.getGameId()))
                                                                                                                           .put("content", q.getContent())
                                                                                                                           .put("correctAnswerId", String.valueOf(q.getCorrectAnswerId()))
                                                                                                                           .build())
                                                                                   .build())
                                           .toCompletableFuture()
                                           .join();
            closeQuietly(result);
        }
    }

    public void deleteByGameId(int gameId) {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "DELETE FROM %s WHERE gameId = :gid".formatted(QUESTIONS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("gid", String.valueOf(gameId))
                                                                               .build()
                                       )
                                       .toCompletableFuture()
                                       .join();
        closeQuietly(result);
    }

    private List<khitto.model.Question> findAllInternal() {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute("SELECT * FROM %s".formatted(QUESTIONS_COLLECTION_NAME))
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(ItemToModel::question)
                         .collect(Collectors.toList());
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

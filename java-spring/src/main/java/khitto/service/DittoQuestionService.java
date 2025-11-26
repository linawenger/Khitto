package khitto.service;

import java.io.IOException;
import java.util.List;
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
public class DittoQuestionService {

    private static final String QUESTIONS_COLLECTION_NAME = "questions";
    private final DittoService dittoService;

    public DittoQuestionService(DittoService dittoService) {
        this.dittoService = dittoService;

        Ditto ditto = dittoService.getDitto();
        try {
            ditto.getSync().registerSubscription("SELECT * FROM %s".formatted(QUESTIONS_COLLECTION_NAME));
        } catch (DittoError ignored) {}
    }

    public java.util.List<khitto.model.Question> findByGameId(int gameId) {
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
                         .map(this::itemToModelQuestion)
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

    public void saveAll(java.util.List<khitto.model.Question> questions) {
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

    @Nonnull
    public Flux<java.util.List<khitto.model.Question>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY content ASC".formatted(QUESTIONS_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToModelQuestion).toList())
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

    private java.util.List<khitto.model.Question> findAllInternal() {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute("SELECT * FROM %s".formatted(QUESTIONS_COLLECTION_NAME))
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(this::itemToModelQuestion)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    private khitto.model.Question itemToModelQuestion(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();

        String idStr       = value.get("id")       != null ? value.get("id").getString()       : "0";
        String gameIdStr   = value.get("gameId")   != null ? value.get("gameId").getString()   : "0";
        String content     = value.get("content")  != null ? value.get("content").getString()  : "";
        String correctStr  = value.get("correctAnswerId") != null ? value.get("correctAnswerId").getString() : "0";

        int id       = parseIntSafe(idStr, 0);
        int gameId   = parseIntSafe(gameIdStr, 0);
        int correct  = parseIntSafe(correctStr, 0);

        return new khitto.model.Question(id, gameId, content, correct);
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
        }
        return fallback;
    }

    private void closeQuietly(DittoQueryResult result) {
        if (result == null) return;
        try {
            result.close();
        } catch (IOException e) {
            // ignore
        }
    }
}

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
public class DittoAnswerService {

    private static final String ANSWERS_COLLECTION_NAME = "answers";
    private final DittoService dittoService;

    public DittoAnswerService(DittoService dittoService) {
        this.dittoService = dittoService;
        Ditto ditto = dittoService.getDitto();
        try {
            ditto.getSync().registerSubscription("SELECT * FROM %s".formatted(ANSWERS_COLLECTION_NAME));
        } catch (DittoError ignored) {}
    }

    public java.util.List<khitto.model.Answer> findByQuestionId(int questionId) {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "SELECT * FROM %s WHERE questionId = :qid".formatted(ANSWERS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("qid", String.valueOf(questionId))
                                                                               .build())
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(this::itemToModelAnswer)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    public int getNextId() {
        List<khitto.model.Answer> all = findAllInternal();
        return all.stream().mapToInt(khitto.model.Answer::getId).max().orElse(0) + 1;
    }

    public void saveAll(java.util.List<khitto.model.Answer> answers) {
        Ditto ditto = dittoService.getDitto();

        for (khitto.model.Answer a : answers) {
            DittoQueryResult result = ditto.getStore()
                                           .execute(
                                                   "INSERT INTO %s DOCUMENTS (:newAnswer)".formatted(ANSWERS_COLLECTION_NAME),
                                                   DittoCborSerializable.Dictionary.buildDictionary()
                                                                                   .put("newAnswer",
                                                                                           DittoCborSerializable.Dictionary.buildDictionary()
                                                                                                                           .put("_id", UUID.randomUUID().toString())
                                                                                                                           .put("id", String.valueOf(a.getId()))
                                                                                                                           .put("questionId", String.valueOf(a.getQuestionId()))
                                                                                                                           .put("content", a.getContent())
                                                                                                                           .build())
                                                                                   .build())
                                           .toCompletableFuture()
                                           .join();
            closeQuietly(result);
        }
    }

    @Nonnull
    public Flux<java.util.List<khitto.model.Answer>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY content ASC".formatted(ANSWERS_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToModelAnswer).toList())
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

    private java.util.List<khitto.model.Answer> findAllInternal() {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute("SELECT * FROM %s".formatted(ANSWERS_COLLECTION_NAME))
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(this::itemToModelAnswer)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    private khitto.model.Answer itemToModelAnswer(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();

        String idStr  = value.get("id")        != null ? value.get("id").getString()        : "0";
        String qidStr = value.get("questionId")!= null ? value.get("questionId").getString(): "0";
        String content= value.get("content")   != null ? value.get("content").getString()   : "";

        int id  = parseIntSafe(idStr, 0);
        int qid = parseIntSafe(qidStr, 0);

        return new khitto.model.Answer(id, qid, content);
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

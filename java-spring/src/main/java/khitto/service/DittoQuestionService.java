package khitto.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.ditto.java.Ditto;
import com.ditto.java.DittoError;
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
    }

    public void addQuestion(@Nonnull String content) {
        dittoService.getDitto().getStore().execute(
                "INSERT INTO %s DOCUMENTS (:newQuestion)".formatted(QUESTIONS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newQuestion",
                                                        DittoCborSerializable.Dictionary.buildDictionary()
                                                                                        .put("_id", UUID.randomUUID().toString())
                                                                                        .put("content", content)
                                                                                        .build())
                                                .build()
        ).toCompletableFuture().join();
    }

    public void updateQuestion(@Nonnull String qid, @Nonnull String newContent) {
        dittoService.getDitto().getStore().execute(
                "UPDATE %s SET content = :newContent WHERE _id = :qid".formatted(QUESTIONS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newContent", newContent)
                                                .put("qid", qid)
                                                .build()
        ).toCompletableFuture().join();
    }

    public void deleteQuestion(@Nonnull String qid) {
        dittoService.getDitto().getStore().execute(
                "DELETE FROM %s WHERE _id = :qid".formatted(QUESTIONS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("qid", qid)
                                                .build()
        ).toCompletableFuture().join();
    }

    @Nonnull
    public Flux<List<Question>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY content ASC".formatted(QUESTIONS_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToQuestion).toList())
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

    private Question itemToQuestion(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();
        return new Question(
                value.get("_id").getString(),
                value.get("content").getString()
        );
    }
}

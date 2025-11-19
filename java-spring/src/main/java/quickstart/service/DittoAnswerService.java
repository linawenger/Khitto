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
public class DittoAnswerService {

    private static final String ANSWERS_COLLECTION_NAME = "answers";
    private final DittoService dittoService;

    public DittoAnswerService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    public void addAnswer(@Nonnull String content, @Nonnull String answer) {
        dittoService.getDitto().getStore().execute(
                "INSERT INTO %s DOCUMENTS (:newAnswer)".formatted(ANSWERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newAnswer",
                                                        DittoCborSerializable.Dictionary.buildDictionary()
                                                                                        .put("_id", UUID.randomUUID().toString())
                                                                                        .put("content", content)
                                                                                        .put("answer", answer)
                                                                                        .build())
                                                .build()
        ).toCompletableFuture().join();
    }

    public void updateAnswer(@Nonnull String aid, @Nonnull String newAnswer) {
        dittoService.getDitto().getStore().execute(
                "UPDATE %s SET answer = :newAnswer WHERE _id = :aid".formatted(ANSWERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newAnswer", newAnswer)
                                                .put("aid", aid)
                                                .build()
        ).toCompletableFuture().join();
    }

    public void updateContent(@Nonnull String aid, @Nonnull String newContent) {
        dittoService.getDitto().getStore().execute(
                "UPDATE %s SET content = :newContent WHERE _id = :aid".formatted(ANSWERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newContent", newContent)
                                                .put("aid", aid)
                                                .build()
        ).toCompletableFuture().join();
    }

    public void deleteAnswer(@Nonnull String aid) {
        dittoService.getDitto().getStore().execute(
                "DELETE FROM %s WHERE _id = :aid".formatted(ANSWERS_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("aid", aid)
                                                .build()
        ).toCompletableFuture().join();
    }

    @Nonnull
    public Flux<List<Answer>> observeAll() {
        final String query = "SELECT * FROM %s ORDER BY content ASC".formatted(ANSWERS_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToAnswer).toList())
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

    private Answer itemToAnswer(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();
        return new Answer(
                value.get("_id").getString(),
                value.get("content").getString(),
                value.get("answer").getString()
        );
    }
}

package com.ditto.example.spring.quickstart.service;

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
public class DittoActiveService {

    private static final String ACTIVE_COLLECTION_NAME = "active";
    private final DittoService dittoService;

    public DittoActiveService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    public void addActive() {
        dittoService.getDitto().getStore().execute(
                "INSERT INTO %s DOCUMENTS (:newActive)".formatted(ACTIVE_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("newActive",
                                                        DittoCborSerializable.Dictionary.buildDictionary()
                                                                                        .put("_id", UUID.randomUUID().toString())
                                                                                        .build())
                                                .build()
        ).toCompletableFuture().join();
    }

    public void deleteActive(@Nonnull String aid) {
        dittoService.getDitto().getStore().execute(
                "DELETE FROM %s WHERE _id = :aid".formatted(ACTIVE_COLLECTION_NAME),
                DittoCborSerializable.Dictionary.buildDictionary()
                                                .put("aid", aid)
                                                .build()
        ).toCompletableFuture().join();
    }

    @Nonnull
    public Flux<List<Active>> observeAll() {
        final String query = "SELECT * FROM %s".formatted(ACTIVE_COLLECTION_NAME);

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);
                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results ->
                        emitter.next(results.getItems().stream().map(this::itemToActive).toList())
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

    private Active itemToActive(@Nonnull DittoQueryResultItem item) {
        var value = item.getValue();
        return new Active(
                value.get("_id").getString()
        );
    }
}

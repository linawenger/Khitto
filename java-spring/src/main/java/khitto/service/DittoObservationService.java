package khitto.service;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ditto.java.Ditto;
import com.ditto.java.DittoError;
import com.ditto.java.DittoQueryResultItem;
import com.ditto.java.DittoStoreObserver;
import com.ditto.java.DittoSyncSubscription;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Component
public class DittoObservationService {

    private static final Logger log = LoggerFactory.getLogger(DittoObservationService.class);
    private final DittoService dittoService;

    public DittoObservationService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    @Nonnull
    public <T> Flux<List<T>> observeList(String query,
                                         Function<DittoQueryResultItem, T> mapper) {
        return observeList(query, query, mapper);
    }

    @Nonnull
    public <T> Flux<List<T>> observeList(String subscriptionQuery,
                                         String displayQuery,
                                         Function<DittoQueryResultItem, T> mapper) {

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription =
                        ditto.getSync().registerSubscription(subscriptionQuery);

                DittoStoreObserver observer =
                        ditto.getStore().registerObserver(displayQuery, results -> {
                            try {
                                List<T> mapped = results.getItems()
                                                        .stream()
                                                        .map(mapper)
                                                        .collect(Collectors.toList());
                                emitter.next(mapped);
                            } catch (Throwable t) {
                                log.error("Error while mapping Ditto results", t);
                                emitter.error(t);
                            }
                        });

                emitter.onDispose(() -> {
                    try {
                        log.info("Disposing Ditto observer for query: {}", displayQuery);
                        observer.close();
                        subscription.close();
                    } catch (IOException e) {
                        log.warn("Error closing Ditto observer/subscription", e);
                    }
                });

            } catch (DittoError e) {
                log.error("Error registering Ditto observer", e);
                emitter.error(e);
            }
        }, FluxSink.OverflowStrategy.LATEST);
    }
}

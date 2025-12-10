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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Component
public class DittoObservationService {

    private final DittoService dittoService;

    public DittoObservationService(DittoService dittoService) {
        this.dittoService = dittoService;
    }

    @Nonnull
    public <T> Flux<List<T>> observeList(String query,
                                         Function<DittoQueryResultItem, T> mapper) {

        return Flux.create(emitter -> {
            Ditto ditto = dittoService.getDitto();
            try {
                DittoSyncSubscription subscription = ditto.getSync().registerSubscription(query);

                DittoStoreObserver observer = ditto.getStore().registerObserver(query, results -> {
                    List<T> mapped = results.getItems()
                                            .stream()
                                            .map(mapper)
                                            .collect(Collectors.toList());
                    emitter.next(mapped);
                });

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
}

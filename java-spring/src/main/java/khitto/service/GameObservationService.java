package khitto.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import khitto.model.Game;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class GameObservationService {

    private static final String SUBSCRIPTION_QUERY =
            "SELECT * FROM games";

    private static final String DISPLAY_QUERY =
            "SELECT * FROM games";

    private final Flux<List<Game>> gamesFlux;

    public GameObservationService(DittoObservationService observationService) {

        this.gamesFlux = observationService
                .observeList(SUBSCRIPTION_QUERY, DISPLAY_QUERY, ItemToModel::game)
                .map(list -> list.stream()
                                 .filter(g -> !g.isDeleted())
                                 .collect(Collectors.toList()))
                .sample(Duration.ofMillis(150))
                .replay(1)
                .refCount(1);
    }

    public Flux<List<Game>> observeGames() {
        return gamesFlux;
    }
}

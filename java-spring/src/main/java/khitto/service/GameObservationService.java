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

    private final DittoObservationService observationService;
    private final Flux<List<Game>> gamesFlux;

    public GameObservationService(DittoObservationService observationService) {
        this.observationService = observationService;

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

    /**
     * Observe single games based on id
     * Subsrciber get their own subscription and store observer
     */
    public Flux<Game> observeGame(String gameId) {
        String query = "SELECT * FROM games WHERE id = '%s'".formatted(gameId);

        return observationService
                .observeList(query, query, ItemToModel::game)
                .map(list -> list.stream().findFirst())
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .distinctUntilChanged(Game::getStatus);
    }


}

package khitto.web.stream;

import java.util.List;
import java.util.stream.Collectors;
import khitto.model.Game;
import khitto.service.GameObservationService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import reactor.core.publisher.Flux;

@Controller
public class GameStreamController {

    private final GameObservationService gameObservationService;
    private final SpringTemplateEngine templateEngine;

    public GameStreamController(GameObservationService gameObservationService,
                                SpringTemplateEngine templateEngine) {
        this.gameObservationService = gameObservationService;
        this.templateEngine = templateEngine;
    }

    @GetMapping(path = "/games/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamGames() {

        return gameObservationService
                .observeGames()
                .map(this::renderGamesFragment)
                .map(html ->
                        ServerSentEvent.<String>builder()
                                       .event("game_list")
                                       .data(html)
                                       .build()
                );
    }

    @GetMapping(path = "/games/{id}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Game>> streamSingleGame(
            @org.springframework.web.bind.annotation.PathVariable("id") String id) {

        return gameObservationService
                .observeGame(id)
                .map(game ->
                        ServerSentEvent.<Game>builder()
                                .event("game")
                                .data(game)
                                .build()
                );
    }

    private String renderGamesFragment(List<Game> games) {
        List<Game> templates = games.stream()
                                    .filter(g -> g.getStatus() == 0)
                                    .collect(Collectors.toList());

        List<Game> activeGames = games.stream()
                                      .filter(g -> g.getStatus() != 0)
                                      .collect(Collectors.toList());

        Context ctx = new Context();
        ctx.setVariable("templates", templates);
        ctx.setVariable("activeGames", activeGames);
        return templateEngine.process("fragments/gameList", ctx);
    }
}

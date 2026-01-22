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
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class GameStreamController {

    private final ObjectMapper objectMapper;

    private final GameObservationService gameObservationService;
    private final SpringTemplateEngine templateEngine;

    public GameStreamController(GameObservationService gameObservationService,
                                SpringTemplateEngine templateEngine,
                                ObjectMapper objectMapper) {
        this.gameObservationService = gameObservationService;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
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
    public Flux<ServerSentEvent<String>> streamSingleGame(
            @org.springframework.web.bind.annotation.PathVariable("id") String id) {

        return gameObservationService
                .observeGame(id)
                .map(game -> {
                    try {
                        return objectMapper.writeValueAsString(game);
                    } catch (Exception e) {
                        // If serialization fails, fail this stream (you'll see it in logs)
                        throw new RuntimeException("Failed to serialize game to JSON", e);
                    }
                })
                .map(json ->
                        ServerSentEvent.<String>builder()
                                       .event("game")
                                       .data(json)
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

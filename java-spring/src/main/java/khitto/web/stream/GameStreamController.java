package khitto.web.stream;

import khitto.model.Game;
import khitto.service.DittoGameService;
import jakarta.annotation.Nonnull;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@RestController
public class GameStreamController {

    @Nonnull
    private final DittoGameService gameService;

    @Nonnull
    private final SpringTemplateEngine templateEngine;

    public GameStreamController(DittoGameService gameService,
                                SpringTemplateEngine templateEngine) {
        this.gameService = gameService;
        this.templateEngine = templateEngine;
    }

    @GetMapping(value = "/games/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamGames() {
        return gameService.observeAll().map(this::renderGamesFragment);
    }

    @Nonnull
    private ServerSentEvent<String> renderGamesFragment(@Nonnull List<Game> games) {
        Context ctx = new Context();
        ctx.setVariable("games", games);

        String html = templateEngine.process(
                "fragments/gameList",
                Set.of("gameListFrag"),
                ctx
        );

        return ServerSentEvent.<String>builder(html)
                              .event("game_list")
                              .build();
    }
}

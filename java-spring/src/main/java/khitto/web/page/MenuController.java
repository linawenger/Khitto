package khitto.web.page;

import java.util.List;
import java.util.stream.Collectors;
import khitto.model.Game;
import khitto.service.DittoAnswerService;
import khitto.service.DittoGameService;
import khitto.service.DittoQuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MenuController extends BaseGameController {

    public MenuController(DittoGameService gameRepo,
                          DittoQuestionService questionRepo,
                          DittoAnswerService answerRepo) {
        super(gameRepo, questionRepo, answerRepo);
    }

    @GetMapping("/")
    public String menu(Model model) {
        List<Game> all = gameRepo.findAll();

        List<Game> templates = all.stream()
                                  .filter(g -> g.getStatus() == 0)
                                  .collect(Collectors.toList());

        List<Game> activeGames = all.stream()
                                    .filter(g -> g.getStatus() != 0)
                                    .collect(Collectors.toList());

        model.addAttribute("templates", templates);
        model.addAttribute("activeGames", activeGames);
        return "menu";
    }

    @GetMapping("/games/import")
    public String importPage() {
        return "import";
    }

    @PostMapping("/games/new")
    public String newGame() {
        String id = gameRepo.getUUID();
        Game g = new Game(id, "", 0, false);
        gameRepo.save(g);
        return "redirect:/maker/" + id;
    }

    @PostMapping("/games/{id}/delete")
    public String deleteGame(@PathVariable String id) {
        gameRepo.delete(id);
        return "redirect:/";
    }
}

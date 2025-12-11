package khitto.web.page;

import java.util.List;
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
        List<Game> games = gameRepo.findAll();
        model.addAttribute("games", games);
        return "menu";
    }

    @PostMapping("/games/new")
    public String newGame() {
        int id = gameRepo.getNextId();
        Game g = new Game(id, "", 0, false);
        gameRepo.save(g);
        return "redirect:/maker/" + id;
    }

    @PostMapping("/games/{id}/delete")
    public String deleteGame(@PathVariable int id) {
        gameRepo.delete(id);
        return "redirect:/";
    }
}

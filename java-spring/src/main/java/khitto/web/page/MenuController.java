package khitto.web.page;

import java.util.*;
import khitto.model.*;
import khitto.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class MenuController extends BaseGameController {

    public MenuController(DittoGameService gameRepo,
                          DittoQuestionService questionRepo,
                          DittoAnswerService answerRepo) {
        super(gameRepo, questionRepo, answerRepo);
    }

    @GetMapping("/")
    public String menu(Model model) {
        List<Game> games = gameRepo.observeAll().blockFirst();
        if (games == null) {
            games = Collections.emptyList();
        }
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

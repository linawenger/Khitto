package khitto.web;

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
        model.addAttribute("games", gameRepo.findAll());
        return "menu";
    }

    @PostMapping("/games/new")
    public String newGame() {
        int id = gameRepo.getNextId();
        Game g = new Game(id, "", 0, false);
        gameRepo.save(g);
        return "redirect:/maker/" + id;
    }
}

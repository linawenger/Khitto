package khitto.web;

import khitto.model.*;
import khitto.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.Comparator;
import java.util.List;

@Controller
public class GamePlayController extends BaseGameController {

    public GamePlayController(DittoGameService gameRepo,
                              DittoQuestionService questionRepo,
                              DittoAnswerService answerRepo) {
        super(gameRepo, questionRepo, answerRepo);
    }

    @GetMapping("/games/{id}")
    public String play(@PathVariable int id) {
        Game game = gameRepo.findById(id).orElseThrow();

        if (!game.isFinished()) {
            return "redirect:/maker/" + id;
        }

        int status = game.getStatus();

        if (status == 0) {
            return "redirect:/games/" + id + "/start";
        }

        List<Question> questions = questionRepo.findByGameId(id)
                                               .stream()
                                               .sorted(Comparator.comparingInt(Question::getId))
                                               .toList();

        int questionScreens = questions.size();
        int maxScreens = questionScreens * 2;

        if (status > maxScreens) {
            game.setStatus(0);
            gameRepo.save(game);
            return "redirect:/";
        }

        if (status % 2 == 1) {
            return "redirect:/games/" + id + "/question/" + status;
        } else {
            return "redirect:/games/" + id + "/result/" + status;
        }
    }

    @GetMapping("/games/{id}/start")
    public String start(@PathVariable int id, Model model) {
        Game game = gameRepo.findById(id).orElseThrow();
        model.addAttribute("game", game);
        return "start";
    }

    @PostMapping("/games/{id}/start")
    public String startGame(@PathVariable int id) {
        Game game = gameRepo.findById(id).orElseThrow();
        game.setStatus(1);
        gameRepo.save(game);
        return "redirect:/games/" + id;
    }

    @GetMapping("/games/{id}/question/{status}")
    public String question(@PathVariable int id,
                           @PathVariable int status,
                           Model model) {
        Game game = gameRepo.findById(id).orElseThrow();

        List<Question> questions = questionRepo.findByGameId(id)
                                               .stream()
                                               .sorted(Comparator.comparingInt(Question::getId))
                                               .toList();

        int index = (status + 1) / 2 - 1;

        if (index < 0 || index >= questions.size()) {
            return "redirect:/games/" + id;
        }

        Question q = questions.get(index);
        List<Answer> answers = answerRepo.findByQuestionId(q.getId());

        model.addAttribute("game", game);
        model.addAttribute("question", q);
        model.addAttribute("answers", answers);
        return "question";
    }

    @PostMapping("/games/{id}/next")
    public String next(@PathVariable int id) {
        Game game = gameRepo.findById(id).orElseThrow();
        game.setStatus(game.getStatus() + 1);
        gameRepo.save(game);
        return "redirect:/games/" + id;
    }

    @GetMapping("/games/{id}/result/{status}")
    public String result(@PathVariable int id,
                         @PathVariable int status,
                         Model model) {
        Game game = gameRepo.findById(id).orElseThrow();
        model.addAttribute("game", game);
        return "result";
    }
}

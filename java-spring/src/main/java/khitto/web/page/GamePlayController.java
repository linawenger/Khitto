package khitto.web.page;

import khitto.model.*;
import khitto.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.Comparator;
import java.util.List;

@Controller
public class GamePlayController extends BaseGameController {

    private final GameInstanceService gameInstanceService;

    public GamePlayController(DittoGameService gameRepo,
                              DittoQuestionService questionRepo,
                              DittoAnswerService answerRepo,
                              GameInstanceService gameInstanceService) {
        super(gameRepo, questionRepo, answerRepo);
        this.gameInstanceService = gameInstanceService;
    }

    private String redirectToHome() {
        return "redirect:/";
    }

    @GetMapping("/games/{id}")
    public String play(@PathVariable String id) {
        Game game = gameRepo.findById(id).orElseThrow();

        if (!game.isFinished()) {return redirectToHome();}

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
            game.setDeleted(true);
            gameRepo.save(game);
            return redirectToHome();
        }

        if (status % 2 == 1) {
            return "redirect:/games/" + id + "/question/" + status;
        } else {
            return "redirect:/games/" + id + "/result/" + status;
        }
    }

    @GetMapping("/games/{id}/start")
    public String start(@PathVariable String id, Model model) {
        Game game = gameRepo.findById(id).orElseThrow();

        if (!game.isFinished() || game.getStatus() != 0) {return redirectToHome();}

        model.addAttribute("game", game);
        return "start";
    }

    @PostMapping("/games/{id}/start")
    public String startGame(@PathVariable String id,
                            @RequestParam(name = "instanceLabel", required = false) String instanceLabel) {

        Game instance = gameInstanceService.createInstanceFromTemplate(id, instanceLabel);

        return "redirect:/games/" + instance.getId();
    }

    @GetMapping("/games/{id}/question/{status}")
    public String question(@PathVariable String id,
                           @PathVariable int status,
                           Model model) {
        Game game = gameRepo.findById(id).orElseThrow();

        if (!game.isFinished() || game.getStatus() == 0) {return redirectToHome();}

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
    public String next(@PathVariable String id) {
        Game game = gameRepo.findById(id).orElseThrow();

        if (!game.isFinished() || game.getStatus() == 0) {return redirectToHome();}

        game.setStatus(game.getStatus() + 1);
        gameRepo.save(game);
        return "redirect:/games/" + id;
    }

    @GetMapping("/games/{id}/result/{status}")
    public String result(@PathVariable String id,
                         @PathVariable int status,
                         Model model) {

        Game game = gameRepo.findById(id).orElseThrow();

        if (!game.isFinished() || game.getStatus() == 0) {
            return redirectToHome();
        }

        List<Question> questions = questionRepo.findByGameId(id)
                                               .stream()
                                               .sorted(Comparator.comparingInt(Question::getId))
                                               .toList();

        int index = status / 2 - 1;

        if (index < 0 || index >= questions.size()) {
            return "redirect:/games/" + id;
        }

        Question q = questions.get(index);
        List<Answer> answers = answerRepo.findByQuestionId(q.getId());

        String correctUid = q.getCorrectAnswerUid();

        model.addAttribute("game", game);
        model.addAttribute("answers", answers);
        model.addAttribute("correctAnswerUid", correctUid);

        return "result";
    }

}

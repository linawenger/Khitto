package khitto.web;

import khitto.model.*;
import khitto.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MakerController extends BaseGameController {

    public MakerController(DittoGameService gameRepo,
                           DittoQuestionService questionRepo,
                           DittoAnswerService answerRepo) {
        super(gameRepo, questionRepo, answerRepo);
    }

    @GetMapping("/maker/{id}")
    public String maker(@PathVariable int id, Model model) {
        Game game = gameRepo.findById(id).orElseThrow();
        model.addAttribute("game", game);
        return "maker/maker";
    }

    @PostMapping("/maker/{id}/publish")
    public String publish(@PathVariable int id,
                          @RequestParam String name,
                          @RequestParam(value = "q[]", required = false) List<String> questions,
                          @RequestParam(value = "a1[]", required = false) List<String> a1,
                          @RequestParam(value = "a2[]", required = false) List<String> a2,
                          @RequestParam(value = "a3[]", required = false) List<String> a3,
                          @RequestParam(value = "a4[]", required = false) List<String> a4,
                          @RequestParam(value = "correct[]", required = false) List<Integer> correct) {

        Game game = gameRepo.findById(id).orElseThrow();
        game.setName(name);
        game.setFinished(true);
        gameRepo.save(game);

        List<String> qSafe      = (questions != null) ? questions : List.of();
        List<String> a1Safe     = (a1 != null) ? a1 : List.of();
        List<String> a2Safe     = (a2 != null) ? a2 : List.of();
        List<String> a3Safe     = (a3 != null) ? a3 : List.of();
        List<String> a4Safe     = (a4 != null) ? a4 : List.of();
        List<Integer> correctSafe = (correct != null) ? correct : List.of();

        List<Question> newQuestions = new ArrayList<>();
        List<Answer> newAnswers = new ArrayList<>();

        int qId = questionRepo.getNextOddId();

        for (int i = 0; i < qSafe.size(); i++) {
            String qtext = qSafe.get(i);
            if (qtext == null || qtext.isBlank()) {
                continue;
            }

            String ans1 = (a1Safe.size() > i) ? a1Safe.get(i) : "";
            String ans2 = (a2Safe.size() > i) ? a2Safe.get(i) : "";
            String ans3 = (a3Safe.size() > i) ? a3Safe.get(i) : "";
            String ans4 = (a4Safe.size() > i) ? a4Safe.get(i) : "";

            int correctIndex = (correctSafe.size() > i) ? correctSafe.get(i) : 1; // fallback: 1

            int baseAnswerId = answerRepo.getNextId();
            List<String> answerTexts = List.of(ans1, ans2, ans3, ans4);
            int correctAnswerId = -1;
            for (int j = 0; j < 4; j++) {
                int thisAnswerId = baseAnswerId + j;
                Answer ans = new Answer(thisAnswerId, qId, answerTexts.get(j));
                newAnswers.add(ans);
                if (j + 1 == correctIndex) {
                    correctAnswerId = thisAnswerId;
                }
            }

            Question q = new Question(qId, id, qtext, correctAnswerId);
            newQuestions.add(q);
            qId += 2;
        }

        if (!newQuestions.isEmpty()) {
            questionRepo.saveAll(newQuestions);
        }
        if (!newAnswers.isEmpty()) {
            answerRepo.saveAll(newAnswers);
        }

        return "redirect:/";
    }

    @PostMapping("/maker/{id}/cancel")
    public String cancel(@PathVariable int id) {
        Game game = gameRepo.findById(id).orElseThrow();
        if (!game.isFinished()) {
            gameRepo.delete(id);
        }
        return "redirect:/";
    }
}

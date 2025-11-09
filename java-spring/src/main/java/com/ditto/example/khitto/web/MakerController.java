package com.ditto.example.khitto.web;

import com.ditto.example.khitto.model.*;
import com.ditto.example.khitto.repo.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MakerController {

    private final GameCsvRepository gameRepo;
    private final QuestionCsvRepository questionRepo;
    private final AnswerCsvRepository answerRepo;

    public MakerController(GameCsvRepository gameRepo,
                           QuestionCsvRepository questionRepo,
                           AnswerCsvRepository answerRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
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
                          @RequestParam("q[]") List<String> questions,
                          @RequestParam("a1[]") List<String> a1,
                          @RequestParam("a2[]") List<String> a2,
                          @RequestParam("a3[]") List<String> a3,
                          @RequestParam("a4[]") List<String> a4,
                          @RequestParam("correct[]") List<Integer> correct) {

        Game game = gameRepo.findById(id).orElseThrow();
        game.setName(name);
        game.setFinished(true);
        gameRepo.save(game);

        List<Question> newQuestions = new ArrayList<>();
        List<Answer> newAnswers = new ArrayList<>();

        int qId = questionRepo.getNextOddId();
        for (int i = 0; i < questions.size(); i++) {
            String qtext = questions.get(i);
            if (qtext == null || qtext.isBlank()) continue;

            int baseAnswerId = answerRepo.getNextId();
            int correctIndex = correct.get(i);

            List<String> answerTexts = List.of(a1.get(i), a2.get(i), a3.get(i), a4.get(i));
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

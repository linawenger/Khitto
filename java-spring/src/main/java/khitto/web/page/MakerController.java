package khitto.web.page;

import khitto.model.*;
import khitto.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.*;

@Controller
public class MakerController extends BaseGameController {

    public MakerController(DittoGameService gameRepo,
                           DittoQuestionService questionRepo,
                           DittoAnswerService answerRepo) {
        super(gameRepo, questionRepo, answerRepo);
    }

    @GetMapping("/maker/import")
    public String importToMaker() {
        String id = gameRepo.getUUID();
        Game game = new Game(id, "", 0, false);
        gameRepo.save(game);

        return "redirect:/maker/" + id;
    }

    @GetMapping("/maker/{id}")
    public String maker(@PathVariable String id, Model model) {
        Game game = gameRepo.findById(id).orElseThrow();

        if (game.isFinished()) {
            game.setFinished(false);
            gameRepo.save(game);
        }

        List<Question> questions = questionRepo.findByGameId(id);
        List<ExistingQuestion> existing = new ArrayList<>();

        for (Question q : questions) {
            List<Answer> answers = answerRepo.findByQuestionId(q.getId());
            answers.sort(Comparator.comparing(Answer::getUid));

            String a1 = !answers.isEmpty() ? answers.get(0).getContent() : "";
            String a2 = answers.size() > 1 ? answers.get(1).getContent() : "";
            String a3 = answers.size() > 2 ? answers.get(2).getContent() : "";
            String a4 = answers.size() > 3 ? answers.get(3).getContent() : "";

            int correctIndex = 1;
            for (int i = 0; i < answers.size() && i < 4; i++) {
                if (Objects.equals(answers.get(i).getUid(), q.getCorrectAnswerUid())) {
                    correctIndex = i + 1;
                    break;
                }
            }

            existing.add(new ExistingQuestion(
                    q.getContent(),
                    a1, a2, a3, a4,
                    correctIndex
            ));
        }

        model.addAttribute("game", game);
        model.addAttribute("existingQuestions", existing);

        return "maker/maker";
    }

    @PostMapping("/maker/{id}/publish")
    public String publish(@PathVariable String id,
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

        List<Question> oldQuestions = questionRepo.findByGameId(id);
        for (Question q : oldQuestions) {
            answerRepo.deleteByQuestionId(q.getId());
        }
        questionRepo.deleteByGameId(id);

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

            List<String> answerTexts = List.of(
                    a1Safe.size() > i ? a1Safe.get(i) : "",
                    a2Safe.size() > i ? a2Safe.get(i) : "",
                    a3Safe.size() > i ? a3Safe.get(i) : "",
                    a4Safe.size() > i ? a4Safe.get(i) : ""
            );

            int correctIndex = (correctSafe.size() > i) ? correctSafe.get(i) : 1;

            List<String> answerUids = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                answerUids.add(UUID.randomUUID().toString());
            }

            for (int j = 0; j < 4; j++) {
                newAnswers.add(new Answer(
                        answerUids.get(j),
                        qId,
                        answerTexts.get(j),
                        0
                ));
            }

            String correctAnswerUid = answerUids.get(correctIndex - 1);
            newQuestions.add(new Question(qId, id, qtext, correctAnswerUid));
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
    public String cancel(@PathVariable String id) {
        Game game = gameRepo.findById(id).orElseThrow();

        boolean hasQuestions = !questionRepo.findByGameId(id).isEmpty();

        if (!hasQuestions) {
            gameRepo.delete(id);
        } else {
            game.setFinished(true);
            gameRepo.save(game);
        }
        return "redirect:/";
    }
}

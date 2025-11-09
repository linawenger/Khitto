package com.ditto.example.khitto.web;

import com.ditto.example.khitto.model.*;
import com.ditto.example.khitto.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class GameController {

    private final GameCsvRepository gameRepo;
    private final QuestionCsvRepository questionRepo;
    private final AnswerCsvRepository answerRepo;

    public GameController(GameCsvRepository gameRepo,
                          QuestionCsvRepository questionRepo,
                          AnswerCsvRepository answerRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }

    // MENU
    @GetMapping("/")
    public String menu(Model model) {
        model.addAttribute("games", gameRepo.findAll());
        return "menu";
    }

    // + gedrückt -> neues Game anlegen und in Maker
    @PostMapping("/games/new")
    public String newGame() {
        int id = gameRepo.getNextId();
        Game g = new Game(id, "Standart", 0, false);
        gameRepo.save(g);
        return "redirect:/maker/" + id;
    }

    // Maker anzeigen
    @GetMapping("/maker/{id}")
    public String maker(@PathVariable int id, Model model) {
        Game game = gameRepo.findById(id).orElseThrow();
        model.addAttribute("game", game);
        return "maker/maker";
    }

    // Maker publish
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

            // Answers anlegen
            int baseAnswerId = answerRepo.getNextId();
            int correctIndex = correct.get(i); // 1-4

            // 4 answers
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

            qId += 2; // nächste ungerade
        }

        if (!newQuestions.isEmpty()) {
            questionRepo.saveAll(newQuestions);
        }
        if (!newAnswers.isEmpty()) {
            answerRepo.saveAll(newAnswers);
        }

        return "redirect:/";
    }

    // Maker -> back: wenn game noch nicht verändert / noch nicht finished -> löschen
    @PostMapping("/maker/{id}/cancel")
    public String cancel(@PathVariable int id) {
        Game game = gameRepo.findById(id).orElseThrow();
        if (!game.isFinished()) {
            gameRepo.delete(id);
        }
        return "redirect:/";
    }

    // Spiel klicken im Menü
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

        // alle Fragen zu diesem Spiel
        List<Question> questions = questionRepo.findByGameId(id)
                                               .stream()
                                               .sorted(Comparator.comparingInt(Question::getId))
                                               .toList();

        // wie viele "Frage-Screens" gibt es?
        int questionScreens = questions.size(); // jede Frage genau 1x

        // unser Status läuft so: 1=Frage1, 2=Result1, 3=Frage2, 4=Result2, ...
        // also maximale Screen-Zahl = questionScreens * 2
        int maxScreens = questionScreens * 2;

        if (status > maxScreens) {
            // Spiel fertig → zurücksetzen
            game.setStatus(0);
            gameRepo.save(game);
            return "redirect:/";
        }

        if (status % 2 == 1) {
            // ungerade → Frage
            return "redirect:/games/" + id + "/question/" + status;
        } else {
            // gerade → Result
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
        game.setStatus(1); // erste Frage (ungerade)
        gameRepo.save(game);
        return "redirect:/games/" + id;
    }

    @GetMapping("/games/{id}/question/{status}")
    public String question(@PathVariable int id,
                           @PathVariable int status,
                           Model model) {
        Game game = gameRepo.findById(id).orElseThrow();

        // alle Fragen zu diesem Spiel holen und sortieren
        List<Question> questions = questionRepo.findByGameId(id)
                                               .stream()
                                               .sorted(Comparator.comparingInt(Question::getId))
                                               .toList();

        // status = 1 → index 0
        // status = 3 → index 1
        // index = (status + 1) / 2 - 1
        int index = (status + 1) / 2 - 1;

        if (index < 0 || index >= questions.size()) {
            // Sicherheitsnetz: dann einfach zurück
            return "redirect:/games/" + id;
        }

        Question q = questions.get(index);
        List<Answer> answers = answerRepo.findByQuestionId(q.getId());

        model.addAttribute("game", game);
        model.addAttribute("question", q);
        model.addAttribute("answers", answers);
        return "question";
    }


    // Antwort ausgewählt → wir könnten hier direkt "ob richtig" ausrechnen,
    // aber für den Anfang bleibts im Frontend, Status erhöhen passiert beim Weiter-Button
    @PostMapping("/games/{id}/next")
    public String next(@PathVariable int id) {
        Game game = gameRepo.findById(id).orElseThrow();
        game.setStatus(game.getStatus() + 1);
        gameRepo.save(game);
        return "redirect:/games/" + id;
    }

    @GetMapping("/games/{id}/result/{status}")
    public String result(@PathVariable int id, @PathVariable int status, Model model) {
        Game game = gameRepo.findById(id).orElseThrow();
        model.addAttribute("game", game);
        return "result";
    }
}

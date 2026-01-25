package khitto.web.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import khitto.model.Answer;
import khitto.model.Game;
import khitto.model.Question;
import khitto.service.DittoAnswerService;
import khitto.service.DittoGameService;
import khitto.service.DittoQuestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.*;

@Controller
public class GameExportController {
    private final DittoGameService gameRepo;
    private final DittoQuestionService questionRepo;
    private final DittoAnswerService answerRepo;

    public GameExportController(DittoGameService gameRepo,
                                DittoQuestionService questionRepo,
                                DittoAnswerService answerRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }

    @GetMapping("/games/{id}/export")
    public ResponseEntity<byte[]> exportGame(@PathVariable String id) throws Exception {

        Game game = gameRepo.findById(id).orElseThrow();

        String fileName = sanitizeFileName(game.getName());
        if (fileName.isBlank()) {
            fileName = "game";
        }

        List<Question> questions = questionRepo.findByGameId(id);
        List<Map<String, Object>> export = new ArrayList<>();

        for (Question q : questions) {

            List<Answer> answers = answerRepo.findByQuestionId(q.getId());

            Answer correct = answers.stream()
                                    .filter(a -> a.getUid().equals(q.getCorrectAnswerUid()))
                                    .findFirst()
                                    .orElse(null);

            if (correct == null) continue;

            List<String> incorrect = answers.stream()
                                            .filter(a -> !a.getUid().equals(q.getCorrectAnswerUid()))
                                            .map(Answer::getContent)
                                            .toList();

            if (incorrect.size() < 3) continue;

            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("type", "multiple");
            obj.put("question", q.getContent());
            obj.put("correct_answer", correct.getContent());
            obj.put("incorrect_answers", incorrect.subList(0, 3));

            export.add(obj);
        }

        ObjectMapper mapper = new ObjectMapper();
        byte[] json = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(export);

        return ResponseEntity.ok()
                             .header(
                                     "Content-Disposition",
                                     "attachment; filename=\"" + fileName + ".json\""
                             )
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(json);
    }

    private String sanitizeFileName(String name) {
        return name
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
    }
}
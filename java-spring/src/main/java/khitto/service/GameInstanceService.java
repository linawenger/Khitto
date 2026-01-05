package khitto.service;

import khitto.model.Answer;
import khitto.model.Game;
import khitto.model.Question;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameInstanceService {

    private final DittoGameService gameRepo;
    private final DittoQuestionService questionRepo;
    private final DittoAnswerService answerRepo;

    public GameInstanceService(DittoGameService gameRepo,
                               DittoQuestionService questionRepo,
                               DittoAnswerService answerRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }

    public Game createInstanceFromTemplate(String templateId, String label) {
        Game template = gameRepo.findById(templateId).orElseThrow();

        String instanceId = gameRepo.getUUID();

        String baseName = template.getName() != null ? template.getName() : "";
        String suffix = (label == null) ? "" : label.trim();
        String instanceName = baseName;
        if (!suffix.isEmpty()) {
            instanceName = baseName.isEmpty() ? suffix : baseName + "-" + suffix;
        }

        Game instance = new Game(instanceId, instanceName, 1, true, false);
        gameRepo.save(instance);

        List<Question> templateQuestions = questionRepo.findByGameId(templateId)
                                                       .stream()
                                                       .sorted(Comparator.comparingInt(Question::getId))
                                                       .toList();

        if (templateQuestions.isEmpty()) {
            return instance;
        }

        int nextQuestionId = questionRepo.getNextOddId();
        List<Question> newQuestions = new ArrayList<>();
        List<Answer> newAnswers = new ArrayList<>();

        for (Question oldQ : templateQuestions) {
            int newQuestionId = nextQuestionId;
            nextQuestionId += 2;

            Question newQ = new Question(
                    newQuestionId,
                    instance.getId(),
                    oldQ.getContent(),
                    oldQ.getCorrectAnswerUid()
            );
            newQuestions.add(newQ);

            List<Answer> oldAnswers = answerRepo.findByQuestionId(oldQ.getId());
            for (Answer oldA : oldAnswers) {
                String newUid = UUID.randomUUID().toString();
                Answer newA = new Answer(
                        newUid,
                        newQuestionId,
                        oldA.getContent(),
                        0
                );
                newAnswers.add(newA);

                if (Objects.equals(oldA.getUid(), oldQ.getCorrectAnswerUid())) {
                    newQ.setCorrectAnswerUid(newUid);
                }
            }
        }
        questionRepo.saveAll(newQuestions);
        if (!newAnswers.isEmpty()) {
            answerRepo.saveAll(newAnswers);
        }
        return instance;
    }
}

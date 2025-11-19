package khitto.web;

import khitto.repo.GameCsvRepository;
import khitto.repo.QuestionCsvRepository;
import khitto.repo.AnswerCsvRepository;


public abstract class BaseGameController {

    protected final GameCsvRepository gameRepo;
    protected final QuestionCsvRepository questionRepo;
    protected final AnswerCsvRepository answerRepo;

    protected BaseGameController(GameCsvRepository gameRepo,
                                 QuestionCsvRepository questionRepo,
                                 AnswerCsvRepository answerRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }
}

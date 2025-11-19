package khitto.web;

import khitto.service.*;

public abstract class BaseGameController {

    protected final DittoGameService gameRepo;
    protected final DittoQuestionService questionRepo;
    protected final DittoAnswerService answerRepo;

    protected BaseGameController(DittoGameService gameRepo,
                                 DittoQuestionService questionRepo,
                                 DittoAnswerService answerRepo) {
        this.gameRepo = gameRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }
}

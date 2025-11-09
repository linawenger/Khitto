package com.ditto.example.khitto.web;

import com.ditto.example.khitto.repo.GameCsvRepository;
import com.ditto.example.khitto.repo.QuestionCsvRepository;
import com.ditto.example.khitto.repo.AnswerCsvRepository;


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

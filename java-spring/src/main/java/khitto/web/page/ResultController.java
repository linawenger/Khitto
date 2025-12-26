package khitto.web.page;

import khitto.service.DittoAnswerService;
import khitto.model.Answer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ResultController {

    private final DittoAnswerService answerService;

    public ResultController(DittoAnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping("/api/results/{questionId}")
    public List<Answer> getResults(@PathVariable int questionId) {
        return answerService.findByQuestionId(questionId);
    }

    @GetMapping("/answers/{uid}/count")
    @ResponseBody
    public String getCount(@PathVariable String uid) {
        return answerService.getCountByUid(uid);
    }

}



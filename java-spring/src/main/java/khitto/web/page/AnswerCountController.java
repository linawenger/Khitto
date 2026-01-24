package khitto.web.page;

import khitto.service.DittoAnswerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class AnswerCountController {

    private final DittoAnswerService answerService;

    public AnswerCountController(DittoAnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/answers/{uid}/count")
    @ResponseBody
    public void incrementCount(@PathVariable String uid) {
        answerService.incrementCountWithRetry(uid);
    }
}



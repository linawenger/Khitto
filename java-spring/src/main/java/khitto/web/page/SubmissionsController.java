package khitto.web.page;

import khitto.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@RestController
public class SubmissionsController {
        private final DittoAnswerService dittoAnswerService;

        public SubmissionsController(DittoAnswerService dittoAnswerService) {
            this.dittoAnswerService = dittoAnswerService;
        }

        @PostMapping("/answers/submissions")
        public Map<String, Integer> submissions(@RequestBody List<String> uids) {
            int total = 0;

            for (String uid : uids) {
                String countString = dittoAnswerService.getCountByUid(uid);

                int countInt;
                try {
                    countInt = Integer.parseInt(countString);
                } catch (Exception e) {
                    countInt = 0;
                }

                total += countInt;
            }

            return Map.of("total", total);
        }
    }


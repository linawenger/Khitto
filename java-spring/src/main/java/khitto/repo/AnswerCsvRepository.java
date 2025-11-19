package khitto.repo;

import khitto.csv.CsvUtil;
import khitto.model.Answer;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AnswerCsvRepository {
    private final String path = "data/answers.csv";

    public List<Answer> findAll() {
        return CsvUtil.readAll(path).stream()
                      .map(r -> new Answer(
                              Integer.parseInt(r[0]),
                              Integer.parseInt(r[1]),
                              r[2]
                      )).collect(Collectors.toList());
    }

    public List<Answer> findByQuestionId(int qid) {
        return findAll().stream().filter(a -> a.getQuestionId() == qid).toList();
    }

    public int getNextId() {
        return findAll().stream().mapToInt(Answer::getId).max().orElse(0) + 1;
    }

    public void saveAll(List<Answer> answers) {
        List<Answer> all = findAll();
        all.addAll(answers);
        CsvUtil.writeAll(path, all.stream().map(a -> new String[]{
                String.valueOf(a.getId()),
                String.valueOf(a.getQuestionId()),
                a.getContent()
        }).toList());
    }
}
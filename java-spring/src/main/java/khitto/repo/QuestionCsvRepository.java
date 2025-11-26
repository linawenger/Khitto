package khitto.repo;

import khitto.csv.CsvUtil;
import khitto.model.Question;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class QuestionCsvRepository {
    private final String path = "data/questions.csv";

    public List<Question> findAll() {
        return CsvUtil.readAll(path).stream()
                      .map(r -> new Question(
                              Integer.parseInt(r[0]),
                              Integer.parseInt(r[1]),
                              r[2],
                              Integer.parseInt(r[3])
                      )).collect(Collectors.toList());
    }

    public List<Question> findByGameId(int gameId) {
        return findAll().stream().filter(q -> q.getGameId() == gameId).toList();
    }

    public int getNextOddId() {
        int max = findAll().stream().mapToInt(Question::getId).max().orElse(-1);
        // erste Frage soll 1 sein
        int next = max < 1 ? 1 : max + 2;
        return next % 2 == 1 ? next : next + 1;
    }

    public void saveAll(List<Question> questions) {
        List<Question> all = findAll();
        all.addAll(questions);
        CsvUtil.writeAll(path, all.stream().map(q -> new String[]{
                String.valueOf(q.getId()),
                String.valueOf(q.getGameId()),
                q.getContent(),
                String.valueOf(q.getCorrectAnswerId())
        }).toList());
    }
}
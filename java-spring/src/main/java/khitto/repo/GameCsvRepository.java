package khitto.repo;

import java.util.*;
import java.util.stream.Collectors;
import khitto.csv.CsvUtil;
import khitto.model.Game;
import org.springframework.stereotype.Repository;

@Repository
public class GameCsvRepository {

    private final String path = "data/games.csv";

    public List<Game> findAll() {
        return CsvUtil.readAll(path).stream()
                      .map(r -> new Game(
                              Integer.parseInt(r[0]),
                              r[1],
                              Integer.parseInt(r[2]),
                              Boolean.parseBoolean(r[3])
                      )).collect(Collectors.toList());
    }

    public Optional<Game> findById(int id) {
        return findAll().stream().filter(g -> g.getId() == id).findFirst();
    }

    public void save(Game game) {
        List<Game> all = findAll();
        boolean exists = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == game.getId()) {
                all.set(i, game);
                exists = true;
                break;
            }
        }
        if (!exists) {
            all.add(game);
        }
        CsvUtil.writeAll(path, all.stream().map(g -> new String[]{
                String.valueOf(g.getId()),
                g.getName(),
                String.valueOf(g.getStatus()),
                String.valueOf(g.isFinished())
        }).toList());
    }

    public void delete(int id) {
        List<Game> all = findAll().stream()
                                  .filter(g -> g.getId() != id)
                                  .collect(Collectors.toList());
        CsvUtil.writeAll(path, all.stream().map(g -> new String[]{
                String.valueOf(g.getId()),
                g.getName(),
                String.valueOf(g.getStatus()),
                String.valueOf(g.isFinished())
        }).toList());
    }

    public int getNextId() {
        return findAll().stream().mapToInt(Game::getId).max().orElse(0) + 1;
    }
}
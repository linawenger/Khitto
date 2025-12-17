package khitto.service;

import com.ditto.java.DittoQueryResultItem;
import com.ditto.java.serialization.DittoCborSerializable;
import khitto.model.Game;
import khitto.model.Question;
import khitto.model.Answer;

public final class ItemToModel {

    private ItemToModel() {
    }

    private static String getString(DittoCborSerializable.Dictionary value,
                                    String key) {
        var v = value.get(key);
        return v.getString();
    }

    private static int getInt(DittoCborSerializable.Dictionary value,
                              String key) {
        var v = value.get(key);
        try {
            return Integer.parseInt(v.getString());
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean getBool(DittoCborSerializable.Dictionary value,
                                   String key) {
        var v = value.get(key);
        return Boolean.parseBoolean(v.getString());
    }

    public static Game game(DittoQueryResultItem item) {
        var value = item.getValue();

        String id         = getString(value, "id");
        String name    = getString(value, "name");
        int status     = getInt(value, "status");
        boolean finished = getBool(value, "finished");
        boolean deleted  = getBool(value, "deleted");

        return new Game(id, name, status, finished, deleted);
    }

    public static Question question(DittoQueryResultItem item) {
        var value = item.getValue();

        int id      = getInt(value, "id");
        String gameId  = getString(value, "gameId");
        String text = getString(value, "content");
        int correct = getInt(value, "correctAnswerId");

        return new Question(id, gameId, text, correct);
    }

    public static Answer answer(DittoQueryResultItem item) {
        var value = item.getValue();

        int id         = getInt(value, "id");
        int questionId = getInt(value, "questionId");
        String text    = getString(value, "content");

        return new Answer(id, questionId, text);
    }
}

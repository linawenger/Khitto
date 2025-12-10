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
                                    String key,
                                    String fallback) {
        var v = value.get(key);
        return (v != null) ? v.getString() : fallback;
    }

    private static int getInt(DittoCborSerializable.Dictionary value,
                              String key,
                              int fallback) {
        var v = value.get(key);
        if (v == null) return fallback;
        try {
            return Integer.parseInt(v.getString());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean getBool(DittoCborSerializable.Dictionary value,
                                   String key,
                                   boolean fallback) {
        var v = value.get(key);
        if (v == null) return fallback;
        return Boolean.parseBoolean(v.getString());
    }

    public static Game game(DittoQueryResultItem item) {
        var value = item.getValue();

        int id         = getInt(value, "id", 0);
        String name    = getString(value, "name", "");
        int status     = getInt(value, "status", 0);
        boolean finished = getBool(value, "finished", false);
        boolean deleted  = getBool(value, "deleted", false);

        return new Game(id, name, status, finished, deleted);
    }

    public static Question question(DittoQueryResultItem item) {
        var value = item.getValue();

        int id      = getInt(value, "id", 0);
        int gameId  = getInt(value, "gameId", 0);
        String text = getString(value, "content", "");
        int correct = getInt(value, "correctAnswerId", 0);

        return new Question(id, gameId, text, correct);
    }

    public static Answer answer(DittoQueryResultItem item) {
        var value = item.getValue();

        int id         = getInt(value, "id", 0);
        int questionId = getInt(value, "questionId", 0);
        String text    = getString(value, "content", "");

        return new Answer(id, questionId, text);
    }
}

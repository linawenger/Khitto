package khitto.model;

public class Question {
    private final int id;
    private final String gameId;
    private final String content;
    private String correctAnswerUid;

    public Question(int id, String gameId, String content, String correctAnswerUid) {
        this.id = id;
        this.gameId = gameId;
        this.content = content;
        this.correctAnswerUid = correctAnswerUid;
    }

    public int getId() { return id; }
    public String getGameId() { return gameId; }
    public String getContent() { return content; }
    public String getCorrectAnswerUid() { return correctAnswerUid; }
    public void setCorrectAnswerUid(String newId) {this.correctAnswerUid = newId; }
}

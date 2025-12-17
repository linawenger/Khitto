package khitto.model;

public class Question {
    private int id;
    private String gameId;
    private String content;
    private int correctAnswerId;

    public Question(int id, String gameId, String content, int correctAnswerId) {
        this.id = id;
        this.gameId = gameId;
        this.content = content;
        this.correctAnswerId = correctAnswerId;
    }

    public int getId() { return id; }
    public String getGameId() { return gameId; }
    public String getContent() { return content; }
    public int getCorrectAnswerId() { return correctAnswerId; }
}

package khitto.model;

public class Answer {
    private int id;
    private int questionId;
    private String content;
    private int count;

    public Answer(int id, int questionId, String content, int count) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.count = count;
    }

    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public int getCount() { return count; }
    public void setCount(int count) {
        this.count = count;
    }
}
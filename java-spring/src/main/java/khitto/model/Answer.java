package khitto.model;

public class Answer {
    private String uid;
    private int questionId;
    private String content;
    private int count;

    public Answer(String uid, int questionId, String content, int count) {
        this.uid = uid;
        this.questionId = questionId;
        this.content = content;
        this.count = count;
    }

    public String getUid() {return uid;}
    public void setUid(String uid) {this.uid = uid;}

    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }

    public int getCount() { return count; }
    public void setCount(int count) {this.count = count;}
}
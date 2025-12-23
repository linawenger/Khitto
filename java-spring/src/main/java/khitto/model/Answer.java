package khitto.model;

public class Answer {
    private String uid;
    private int id;
    private int questionId;
    private String content;
    private int count;

//    public Answer(int id, int questionId, String content, int count) {
//        this.uid = null;
//        this.id = id;
//        this.questionId = questionId;
//        this.content = content;
//        this.count = count;
//    }

    public Answer(String uid, int id, int questionId, String content, int count) {
        this.uid = uid;
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.count = count;
    }

    public String getUid() {return uid;}
    public void setUid(String uid) {this.uid = uid;}

    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }

    public int getCount() { return count; }
    public void setCount(int count) {this.count = count;}
}
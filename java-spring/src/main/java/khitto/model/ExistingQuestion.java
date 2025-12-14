package khitto.model;

public class ExistingQuestion {
    private String question;
    private String a1;
    private String a2;
    private String a3;
    private String a4;
    private int correctIndex;

    public ExistingQuestion(String question, String a1, String a2, String a3, String a4, int correctIndex) {
        this.question = question;
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.correctIndex = correctIndex;
    }

    public String getQuestion() { return question; }
    public String getA1() { return a1; }
    public String getA2() { return a2; }
    public String getA3() { return a3; }
    public String getA4() { return a4; }
    public int getCorrectIndex() { return correctIndex; }
}
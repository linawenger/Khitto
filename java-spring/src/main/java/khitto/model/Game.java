package khitto.model;

public class Game {
    private String id;
    private String name;
    private int status;
    private boolean finished;
    private boolean deleted;

    public Game(String id, String name, int status, boolean finished) {
        this(id, name, status, finished, false);
    }

    public Game(String id, String name, int status, boolean finished, boolean deleted) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.finished = finished;
        this.deleted = deleted;
    }

    public Game() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

package prepforge.model;

public abstract class Question {

    public static final String EASY = "EASY";
    public static final String MEDIUM = "MEDIUM";
    public static final String HARD = "HARD";

    private int id;
    private String text;
    private String topic;
    private String difficulty;

    protected Question(int id, String text, String topic, String difficulty) {
        this.id = id;
        setText(text);
        setTopic(topic);
        setDifficulty(difficulty);
    }

    protected Question(String text, String topic, String difficulty) {
        this(0, text, topic, difficulty);
    }

    public abstract String getType();
    public abstract int getMaxMarks();
    public abstract String display();

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Question text cannot be empty");
        }
        this.text = text.trim();
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be empty");
        }
        this.topic = topic.trim();
    }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty cannot be null");
        }
        String d = difficulty.trim().toUpperCase();
        if (!d.equals(EASY) && !d.equals(MEDIUM) && !d.equals(HARD)) {
            throw new IllegalArgumentException("Difficulty must be EASY, MEDIUM or HARD");
        }
        this.difficulty = d;
    }

    @Override
    public String toString() {
        return getType() + "#" + id + " [" + topic + ", " + difficulty + "]";
    }
}
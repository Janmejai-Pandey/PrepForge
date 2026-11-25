package prepforge.util;

import java.io.*;
import java.util.*;
import prepforge.model.*;

public class StorageManager {

    private static final String FILE_NAME = "prepforge_state.dat";

    public static class PersistentState implements Serializable {
        private static final long serialVersionUID = 1L;

        public Map<Integer, User> userDatabase = new HashMap<>();
        public Map<String, User> emailIndex = new HashMap<>();
        public Map<Integer, Skill> skillCatalog = new HashMap<>();
        public int nextUserId = 10;
        public int nextSkillId = 10;

        public Map<Integer, Company> companies = new HashMap<>();
        public Map<Integer, JobRole> jobRoles = new HashMap<>();
        public Map<Integer, Set<Integer>> studentApplications = new HashMap<>();
        public List<ApplicationRecord> applicationRecords = new ArrayList<>();
        public int companyIdCounter = 100;
        public int roleIdCounter = 200;
        public int applicationIdCounter = 500;

        public Map<Integer, Question> questionStore = new HashMap<>();
        public int nextQuestionId = 50;

        public Map<Integer, Interview> interviewStore = new HashMap<>();
        public Map<Integer, Result> resultStore = new HashMap<>();
        public Map<Integer, List<Result>> studentResults = new HashMap<>();
        public Map<Integer, Map<String, Double>> studentSkillScores = new HashMap<>();
        public Map<Integer, List<String>> studentRecommendations = new HashMap<>();
        public int nextInterviewId = 100;
        public int nextResultId = 500;
    }

    private static PersistentState currentState = null;

    public static synchronized PersistentState getState() {
        if (currentState == null) {
            loadState();
        }
        return currentState;
    }

    public static synchronized void saveState() {
        if (currentState == null) return;
        File file = new File(FILE_NAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(currentState);
        } catch (Exception e) {
            System.err.println("Failed to persist state: " + e.getMessage());
        }
    }

    private static synchronized void loadState() {
        File file = new File(FILE_NAME);
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                currentState = (PersistentState) ois.readObject();
                return;
            } catch (Exception e) {
                System.err.println("Could not load saved state, seeding fresh defaults: " + e.getMessage());
            }
        }
        currentState = new PersistentState();
        DataSeeder.seed(currentState);
        saveState();
    }
}

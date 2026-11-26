package prepforge.util;

import java.io.Serializable;
import java.util.*;
import prepforge.model.*;

public class StorageManager {

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

}

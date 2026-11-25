package prepforge.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import prepforge.evaluator.Evaluator;
import prepforge.exception.PrepForgeException;
import prepforge.model.*;

public class DataSeeder {

    private DataSeeder() {
    }

    public static void seed(StorageManager.PersistentState state) {
        if (state == null || !state.userDatabase.isEmpty()) {
            return;
        }

        Map<String, Skill> skills = loadSkills(state);
        Map<String, Student> students = loadUsers(state, skills);
        Map<Integer, JobRole> roles = loadCompaniesAndRoles(state, skills);
        List<Question> questions = loadQuestions(state);
        loadApplications(state, students, roles);
        seedInitialAnalyticsFromLoadedQuestions(state, students, questions);
    }

    private static Map<String, Skill> loadSkills(StorageManager.PersistentState state) {
        Map<String, Skill> byName = new HashMap<>();
        int maxId = 0;
        for (List<String> row : readCsv("/data/skills.csv")) {
            if (row.size() < 3) continue;
            int id = Integer.parseInt(row.get(0).trim());
            String name = row.get(1).trim();
            String category = row.get(2).trim();
            Skill skill = new Skill(id, name, category);
            state.skillCatalog.put(id, skill);
            byName.put(name.toLowerCase(), skill);
            if (id > maxId) maxId = id;
        }
        state.nextSkillId = maxId + 1;
        return byName;
    }

    private static Map<String, Student> loadUsers(StorageManager.PersistentState state, Map<String, Skill> skills) {
        Map<String, Student> byEmail = new HashMap<>();
        int maxId = 0;
        for (List<String> row : readCsv("/data/users.csv")) {
            if (row.size() < 7) continue;
            String userType = row.get(0).trim().toUpperCase();
            int id = Integer.parseInt(row.get(1).trim());
            String name = row.get(2).trim();
            String email = row.get(3).trim();
            String rawPass = row.get(4).trim();
            String deptOrBranch = row.get(5).trim();
            String codeOrEnroll = row.get(6).trim();

            String hash;
            try {
                hash = PasswordHasher.hashPassword(rawPass);
            } catch (PrepForgeException e) {
                hash = "";
            }

            if ("ADMIN".equals(userType)) {
                Admin admin = new Admin(id, name, email, deptOrBranch, codeOrEnroll);
                admin.setUserId(id);
                admin.setPasswordHash(hash);
                state.userDatabase.put(id, admin);
                state.emailIndex.put(email.toLowerCase(), admin);
            } else if ("STUDENT".equals(userType)) {
                double cgpa = row.size() > 7 && !row.get(7).trim().isEmpty()
                        ? Double.parseDouble(row.get(7).trim()) : 0.0;
                Student student = new Student(id, name, email, codeOrEnroll, deptOrBranch, cgpa);
                student.setUserId(id);
                student.setPasswordHash(hash);

                if (row.size() > 8 && !row.get(8).trim().isEmpty()) {
                    for (String pair : row.get(8).split(";")) {
                        String[] parts = pair.split(":");
                        if (parts.length == 2) {
                            Skill sk = skills.get(parts[0].trim().toLowerCase());
                            if (sk != null) {
                                student.addSkill(sk, parts[1].trim());
                            }
                        }
                    }
                }
                state.userDatabase.put(id, student);
                state.emailIndex.put(email.toLowerCase(), student);
                byEmail.put(email.toLowerCase(), student);
            }

            if (id > maxId) maxId = id;
        }
        state.nextUserId = maxId + 1;
        return byEmail;
    }

    private static Map<Integer, JobRole> loadCompaniesAndRoles(StorageManager.PersistentState state,
                                                               Map<String, Skill> skills) {
        int maxCompId = 0;
        for (List<String> row : readCsv("/data/companies.csv")) {
            if (row.size() < 5) continue;
            int id = Integer.parseInt(row.get(0).trim());
            String name = row.get(1).trim();
            String desc = row.get(2).trim();
            String loc = row.get(3).trim();
            String ind = row.get(4).trim();
            Company comp = new Company(id, name, desc, loc, ind);
            state.companies.put(id, comp);
            if (id > maxCompId) maxCompId = id;
        }
        state.companyIdCounter = maxCompId + 1;

        Map<Integer, JobRole> rolesById = new HashMap<>();
        int maxRoleId = 0;
        for (List<String> row : readCsv("/data/job_roles.csv")) {
            if (row.size() < 5) continue;
            int roleId = Integer.parseInt(row.get(0).trim());
            int compId = Integer.parseInt(row.get(1).trim());
            String title = row.get(2).trim();
            double minCgpa = Double.parseDouble(row.get(3).trim());
            String reqSkills = row.get(4).trim();

            Company comp = state.companies.get(compId);
            if (comp == null) continue;

            JobRole role = new JobRole(roleId, title, minCgpa, comp);
            if (!reqSkills.isEmpty()) {
                for (String sName : reqSkills.split(";")) {
                    Skill sk = skills.get(sName.trim().toLowerCase());
                    if (sk != null) {
                        role.addRequiredSkill(sk);
                    }
                }
            }
            comp.addJobRole(role);
            state.jobRoles.put(roleId, role);
            rolesById.put(roleId, role);
            if (roleId > maxRoleId) maxRoleId = roleId;
        }
        state.roleIdCounter = maxRoleId + 1;
        return rolesById;
    }

    private static List<Question> loadQuestions(StorageManager.PersistentState state) {
        List<Question> loaded = new ArrayList<>();
        int qId = 1;
        for (List<String> row : readCsv("/data/questions.csv")) {
            if (row.size() < 4) continue;
            String type = row.get(0).trim().toUpperCase();
            String topic = row.get(1).trim();
            String diff = row.get(2).trim().toUpperCase();
            String text = row.get(3).trim();
            String optionsRaw = row.size() > 4 ? row.get(4).trim() : "";
            String correctIdxRaw = row.size() > 5 ? row.get(5).trim() : "";
            String stmt = row.size() > 6 ? row.get(6).trim() : "";
            String keywordsRaw = row.size() > 7 ? row.get(7).trim() : "";

            Question q = null;
            if ("MCQ".equals(type) && !optionsRaw.isEmpty() && !correctIdxRaw.isEmpty()) {
                List<String> opts = Arrays.asList(optionsRaw.split("\\|\\|"));
                int correctIdx = Integer.parseInt(correctIdxRaw);
                q = new MCQQuestion(qId++, text, topic, diff, opts, correctIdx);
            } else if ("TECHNICAL".equals(type) && !keywordsRaw.isEmpty()) {
                List<String> kw = Arrays.asList(keywordsRaw.split(";"));
                q = new TechnicalQuestion(qId++, text, topic, diff, kw);
            } else if ("CODING".equals(type) && !stmt.isEmpty() && !keywordsRaw.isEmpty()) {
                List<String> kw = Arrays.asList(keywordsRaw.split(";"));
                q = new CodingQuestion(qId++, text, topic, diff, stmt, kw);
            } else if ("HR".equals(type) && !keywordsRaw.isEmpty()) {
                List<String> kw = Arrays.asList(keywordsRaw.split(";"));
                q = new HRQuestion(qId++, text, topic, diff, kw);
            }

            if (q != null) {
                state.questionStore.put(q.getId(), q);
                loaded.add(q);
            }
        }
        state.nextQuestionId = qId;
        return loaded;
    }

    private static void loadApplications(StorageManager.PersistentState state,
                                         Map<String, Student> studentsByEmail,
                                         Map<Integer, JobRole> rolesById) {
        int maxAppId = 0;
        for (List<String> row : readCsv("/data/applications.csv")) {
            if (row.size() < 4) continue;
            int appId = Integer.parseInt(row.get(0).trim());
            String email = row.get(1).trim().toLowerCase();
            int roleId = Integer.parseInt(row.get(2).trim());
            String status = row.get(3).trim().toUpperCase();

            Student student = studentsByEmail.get(email);
            JobRole role = rolesById.get(roleId);
            if (student == null || role == null) continue;

            String companyName = role.getCompany() != null ? role.getCompany().getName() : "Company";
            ApplicationRecord rec = new ApplicationRecord(
                    appId, student.getId(), student.getName(), student.getEmail(),
                    student.getBranch(), student.getCgpa(), role.getRoleId(), role.getTitle(), companyName, status
            );
            state.applicationRecords.add(rec);
            state.studentApplications.computeIfAbsent(student.getId(), k -> new HashSet<>()).add(roleId);
            student.applyToRole(roleId);
            if (appId > maxAppId) maxAppId = appId;
        }
        state.applicationIdCounter = maxAppId + 1;
    }

    private static void seedInitialAnalyticsFromLoadedQuestions(StorageManager.PersistentState state,
                                                                Map<String, Student> studentsByEmail,
                                                                List<Question> questions) {
        if (questions.isEmpty() || studentsByEmail.isEmpty()) return;

        List<Question> easyTech = filterQuestions(questions, "EASY", "MCQ", "TECHNICAL", "CODING");
        List<Question> medTech = filterQuestions(questions, "MEDIUM", "MCQ", "TECHNICAL", "CODING");
        List<Question> easyHr = filterQuestions(questions, "EASY", "HR");

        int interviewId = 1;
        int resultId = 1;

        List<Student> orderedStudents = new ArrayList<>(studentsByEmail.values());
        orderedStudents.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

        if (orderedStudents.size() >= 1 && easyTech.size() >= 4) {
            Student s = orderedStudents.get(0);
            Interview iv1 = new TechnicalInterview(interviewId++, s.getId(), "EASY");
            recordEvaluatedSession(state, iv1, easyTech.subList(0, 4), true, resultId++);

            if (medTech.size() >= 4) {
                Interview iv2 = new TechnicalInterview(interviewId++, s.getId(), "MEDIUM");
                recordEvaluatedSession(state, iv2, medTech.subList(0, 4), false, resultId++);
            }
        }
        if (orderedStudents.size() >= 2 && medTech.size() >= 8) {
            Student s = orderedStudents.get(1);
            Interview iv3 = new TechnicalInterview(interviewId++, s.getId(), "MEDIUM");
            recordEvaluatedSession(state, iv3, medTech.subList(4, 8), true, resultId++);
        }
        if (orderedStudents.size() >= 3 && easyHr.size() >= 3) {
            Student s = orderedStudents.get(2);
            Interview iv4 = new HRInterview(interviewId++, s.getId(), "EASY");
            recordEvaluatedSession(state, iv4, easyHr.subList(0, 3), true, resultId++);
        }

        state.nextInterviewId = interviewId;
        state.nextResultId = resultId;
    }

    private static List<Question> filterQuestions(List<Question> pool, String diff, String... types) {
        List<String> allowed = Arrays.asList(types);
        List<Question> out = new ArrayList<>();
        for (Question q : pool) {
            if (diff.equals(q.getDifficulty()) && allowed.contains(q.getType())) {
                out.add(q);
            }
        }
        return out;
    }

    private static void recordEvaluatedSession(StorageManager.PersistentState state,
                                               Interview interview,
                                               List<Question> qs,
                                               boolean highAccuracy,
                                               int resultId) {
        interview.selectQuestions(qs, qs.size());
        double total = 0;
        List<Question> assigned = interview.getQuestions();
        for (int i = 0; i < assigned.size(); i++) {
            Question q = assigned.get(i);
            String ans = synthesizeResponseFromQuestion(q, highAccuracy || i % 2 == 0);
            interview.addAnswer(q.getId(), ans);
            Evaluator eval = Evaluator.forQuestion(q);
            double marks = eval.evaluate(q, ans);
            interview.recordMarks(q.getId(), marks);
            total += marks;
        }
        interview.markCompleted();
        state.interviewStore.put(interview.getId(), interview);

        Result res = new Result(resultId, interview.getId(), interview.getStudentId(), total, interview.getTotalMarks());
        state.resultStore.put(resultId, res);
        state.studentResults.computeIfAbsent(interview.getStudentId(), k -> new ArrayList<>()).add(res);

        Map<String, double[]> topicScores = new HashMap<>();
        for (Question q : assigned) {
            double[] bucket = topicScores.computeIfAbsent(q.getTopic(), k -> new double[2]);
            bucket[0] += interview.getMarks(q.getId());
            bucket[1] += q.getMaxMarks();
        }
        Map<String, Double> skillAverages = state.studentSkillScores.computeIfAbsent(interview.getStudentId(), k -> new HashMap<>());
        for (Map.Entry<String, double[]> entry : topicScores.entrySet()) {
            double pct = entry.getValue()[1] > 0 ? (entry.getValue()[0] / entry.getValue()[1]) * 100.0 : 0.0;
            double rounded = Math.round(pct * 10.0) / 10.0;
            skillAverages.put(entry.getKey(), rounded);
            if (rounded < 50.0) {
                List<String> recs = state.studentRecommendations.computeIfAbsent(interview.getStudentId(), k -> new ArrayList<>());
                String msg = "Diagnostic alert: Score of " + String.format("%.1f", rounded) + "% in " + entry.getKey() + " requires focused study and practice.";
                if (!recs.contains(msg)) {
                    recs.add(msg);
                }
            }
        }
    }

    private static String synthesizeResponseFromQuestion(Question q, boolean correct) {
        if (q instanceof MCQQuestion) {
            MCQQuestion mcq = (MCQQuestion) q;
            if (correct) return mcq.getCorrectLetter();
            int wrong = (mcq.getCorrectIndex() + 1) % mcq.getOptions().size();
            return String.valueOf((char) ('A' + wrong));
        }
        if (q instanceof KeywordBased) {
            List<String> kw = ((KeywordBased) q).getKeywords();
            if (correct) return String.join(" ", kw);
            return kw.isEmpty() ? "" : kw.get(0);
        }
        return "";
    }

    private static List<List<String>> readCsv(String resourcePath) {
        List<List<String>> records = new ArrayList<>();
        InputStream is = DataSeeder.class.getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("Dataset resource not found: " + resourcePath);
            return records;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder raw = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                raw.append(line).append("\n");
            }
            List<List<String>> parsed = parseRfc4180Csv(raw.toString());
            if (parsed.size() > 1) {
                return parsed.subList(1, parsed.size());
            }
        } catch (IOException e) {
            System.err.println("Failed to read dataset " + resourcePath + ": " + e.getMessage());
        }
        return records;
    }

    private static List<List<String>> parseRfc4180Csv(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    currentRow.add(field.toString());
                    field.setLength(0);
                } else if (c == '\n' || c == '\r') {
                    if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                        i++;
                    }
                    currentRow.add(field.toString());
                    field.setLength(0);
                    if (!(currentRow.size() == 1 && currentRow.get(0).isEmpty())) {
                        rows.add(currentRow);
                    }
                    currentRow = new ArrayList<>();
                } else {
                    field.append(c);
                }
            }
        }
        if (field.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(field.toString());
            rows.add(currentRow);
        }
        return rows;
    }
}

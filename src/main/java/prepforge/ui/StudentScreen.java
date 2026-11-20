package prepforge.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import prepforge.Main;
import prepforge.exception.PrepForgeException;
import prepforge.model.Company;
import prepforge.model.Interview;
import prepforge.model.JobRole;
import prepforge.model.Result;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.service.AnalyticsService;
import prepforge.service.InterviewService;
import prepforge.service.PlacementService;
import prepforge.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * UI Screen: StudentScreen
 * 
 * Contract Owner: Member 4 (UI & Integration)
 * 
 * Provides the central dashboard for students containing all 5 contract tabs:
 * 1. Profile and Skills
 * 2. Start Interview
 * 3. History
 * 4. Recommendations
 * 5. Companies with Eligibility
 */
public class StudentScreen {

    private final Main mainApp;
    private final Student student;
    private final UserService userService;
    private final PlacementService placementService;
    private final InterviewService interviewService;
    private final AnalyticsService analyticsService;

    // Table references for dynamic reloading
    private TableView<SkillRow> skillsTable;
    private TableView<Result> historyTable;
    private TableView<RoleRow> companiesTable;

    public StudentScreen(Main mainApp, Student student, UserService userService,
                         PlacementService placementService, InterviewService interviewService,
                         AnalyticsService analyticsService) {
        this.mainApp = mainApp;
        this.student = student;
        this.userService = userService;
        this.placementService = placementService;
        this.interviewService = interviewService;
        this.analyticsService = analyticsService;
    }

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // ---------------- Top Navigation Bar ----------------
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 24, 16, 24));
        topBar.setStyle("-fx-background-color: #1e293b;");

        Label logo = new Label("PrepForge");
        logo.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        logo.setTextFill(Color.WHITE);

        Label subtitle = new Label("Student Portal");
        subtitle.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label welcomeLabel = new Label("Welcome, " + student.getName() + " (" + student.getBranch() + " | CGPA: " + student.getCgpa() + ")");
        welcomeLabel.setTextFill(Color.web("#cbd5e1"));
        welcomeLabel.setFont(Font.font("SansSerif", FontWeight.NORMAL, 14));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> mainApp.logout());

        topBar.getChildren().addAll(logo, subtitle, spacer, welcomeLabel, logoutBtn);
        root.setTop(topBar);

        // ---------------- Center TabPane (5 Contract Tabs) ----------------
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab1 = new Tab("Profile & Skills", createProfileSkillsTab());
        Tab tab2 = new Tab("Start Interview", createStartInterviewTab());
        Tab tab3 = new Tab("Interview History", createHistoryTab());
        Tab tab4 = new Tab("Recommendations & Analytics", createRecommendationsTab());
        Tab tab5 = new Tab("Companies & Eligibility", createCompaniesTab());

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4, tab5);
        root.setCenter(tabPane);

        return root;
    }

    // =========================================================================
    // TAB 1: Profile & Skills
    // =========================================================================
    private Parent createProfileSkillsTab() {
        HBox box = new HBox(24);
        box.setPadding(new Insets(24));
        box.setAlignment(Pos.TOP_LEFT);

        // Left Card: Academic Profile
        VBox profileCard = new VBox(14);
        profileCard.setPrefWidth(380);
        profileCard.setPadding(new Insets(20));
        styleCard(profileCard);

        Label cardTitle = new Label("Academic Profile");
        cardTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        cardTitle.setStyle("-fx-text-fill: #1e293b;");

        TextField nameTf = new TextField(student.getName());
        TextField emailTf = new TextField(student.getEmail());
        TextField enrollTf = new TextField(student.getEnrollmentNumber() != null ? student.getEnrollmentNumber() : "");
        TextField branchTf = new TextField(student.getBranch() != null ? student.getBranch() : "");
        TextField cgpaTf = new TextField(String.valueOf(student.getCgpa()));

        Button saveProfileBtn = new Button("Update Academic Profile");
        stylePrimaryButton(saveProfileBtn);

        saveProfileBtn.setOnAction(e -> {
            try {
                student.setName(nameTf.getText().trim());
                student.setEmail(emailTf.getText().trim());
                student.setEnrollmentNumber(enrollTf.getText().trim());
                student.setBranch(branchTf.getText().trim());
                student.setCgpa(Double.parseDouble(cgpaTf.getText().trim()));

                Task<Void> updateTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        userService.updateProfile(student);
                        return null;
                    }
                };
                updateTask.setOnSucceeded(ev -> Main.showSuccessAlert("Profile Updated", "Your academic profile has been saved successfully."));
                updateTask.setOnFailed(ev -> Main.showErrorAlert("Update Failed", updateTask.getException().getMessage()));
                new Thread(updateTask).start();
            } catch (NumberFormatException nfe) {
                Main.showErrorAlert("Invalid Input", "CGPA must be a valid number (e.g. 8.5).");
            }
        });

        profileCard.getChildren().addAll(
                cardTitle,
                new Label("Full Name:"), nameTf,
                new Label("Email:"), emailTf,
                new Label("Enrollment Number:"), enrollTf,
                new Label("Academic Branch:"), branchTf,
                new Label("Current CGPA:"), cgpaTf,
                saveProfileBtn
        );

        // Right Card: Skills Management
        VBox skillsCard = new VBox(14);
        HBox.setHgrow(skillsCard, Priority.ALWAYS);
        skillsCard.setPadding(new Insets(20));
        styleCard(skillsCard);

        Label skillsTitle = new Label("Acquired Skills & Proficiency Levels");
        skillsTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));

        skillsTable = new TableView<>();
        TableColumn<SkillRow, String> skillCol = new TableColumn<>("Skill");
        skillCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        skillCol.setPrefWidth(200);

        TableColumn<SkillRow, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category));
        catCol.setPrefWidth(160);

        TableColumn<SkillRow, String> levelCol = new TableColumn<>("Proficiency Level");
        levelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().level));
        levelCol.setPrefWidth(150);

        skillsTable.getColumns().addAll(skillCol, catCol, levelCol);
        loadStudentSkills();

        // Add Skill Controls
        HBox addBox = new HBox(10);
        addBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Skill> skillDropdown = new ComboBox<>();
        skillDropdown.setPromptText("Select Skill to Add");
        try {
            skillDropdown.setItems(FXCollections.observableArrayList(userService.getAllSkills()));
        } catch (Exception ex) {
            // Non-critical fallback
        }

        ComboBox<String> levelDropdown = new ComboBox<>();
        levelDropdown.getItems().addAll("Beginner", "Intermediate", "Advanced");
        levelDropdown.setValue("Intermediate");

        Button addSkillBtn = new Button("Add Skill");
        stylePrimaryButton(addSkillBtn);

        Button removeSkillBtn = new Button("Remove Selected");
        removeSkillBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");

        addSkillBtn.setOnAction(e -> {
            Skill selectedSkill = skillDropdown.getValue();
            String level = levelDropdown.getValue();
            if (selectedSkill == null) {
                Main.showErrorAlert("Select Skill", "Please select a skill from the dropdown.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.addSkill(student.getId(), selectedSkill.getSkillId(), level);
                    student.addSkill(selectedSkill, level);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                loadStudentSkills();
                Main.showSuccessAlert("Skill Added", "Skill '" + selectedSkill.getSkillName() + "' has been added to your profile!");
            });
            task.setOnFailed(ev -> Main.showErrorAlert("Skill Error", task.getException().getMessage()));
            new Thread(task).start();
        });

        removeSkillBtn.setOnAction(e -> {
            SkillRow selected = skillsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Main.showErrorAlert("Select Item", "Please click on a skill row in the table to remove.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.removeSkill(student.getId(), selected.id);
                    student.removeSkill(selected.id);
                    return null;
                }
            };
            task.setOnSucceeded(ev -> loadStudentSkills());
            task.setOnFailed(ev -> Main.showErrorAlert("Remove Error", task.getException().getMessage()));
            new Thread(task).start();
        });

        addBox.getChildren().addAll(skillDropdown, levelDropdown, addSkillBtn, removeSkillBtn);
        skillsCard.getChildren().addAll(skillsTitle, skillsTable, addBox);

        box.getChildren().addAll(profileCard, skillsCard);
        return box;
    }

    private void loadStudentSkills() {
        if (skillsTable == null) return;
        List<SkillRow> rows = FXCollections.observableArrayList();
        for (Skill s : student.getSkills()) {
            String level = student.getSkillLevel(s.getSkillId());
            rows.add(new SkillRow(s.getSkillId(), s.getSkillName(), s.getCategory(), level));
        }
        skillsTable.setItems(FXCollections.observableArrayList(rows));
    }

    public static class SkillRow {
        public int id;
        public String name;
        public String category;
        public String level;
        public SkillRow(int id, String name, String category, String level) {
            this.id = id; this.name = name; this.category = category; this.level = level;
        }
    }

    // =========================================================================
    // TAB 2: Start Interview
    // =========================================================================
    private Parent createStartInterviewTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30, 40, 30, 40));
        container.setAlignment(Pos.TOP_LEFT);

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        styleCard(card);
        card.setMaxWidth(700);

        Label title = new Label("Configure Live Mock Interview");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));

        Label desc = new Label("Select the target domain and interview configuration to start your simulated assessment.");
        desc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Type selection
        VBox typeBox = new VBox(8);
        Label typeLabel = new Label("Interview Type:");
        typeLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));

        RadioButton technicalRb = new RadioButton("Technical Domain (Data Structures, Algorithms, SQL, Java)");
        RadioButton hrRb = new RadioButton("HR & Behavioral (Situational & Leadership Competencies)");
        RadioButton mixedRb = new RadioButton("Comprehensive Mock Interview (Combined Technical + Coding + HR)");
        mixedRb.setSelected(true);

        ToggleGroup typeGroup = new ToggleGroup();
        technicalRb.setToggleGroup(typeGroup);
        hrRb.setToggleGroup(typeGroup);
        mixedRb.setToggleGroup(typeGroup);
        typeBox.getChildren().addAll(typeLabel, technicalRb, hrRb, mixedRb);

        // Difficulty selection
        HBox diffBox = new HBox(16);
        diffBox.setAlignment(Pos.CENTER_LEFT);
        Label diffLabel = new Label("Target Difficulty:");
        ComboBox<String> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll("EASY", "MEDIUM", "HARD");
        diffCombo.setValue("MEDIUM");
        diffBox.getChildren().addAll(diffLabel, diffCombo);

        // Number of questions
        HBox numBox = new HBox(16);
        numBox.setAlignment(Pos.CENTER_LEFT);
        Label numLabel = new Label("Number of Questions:");
        ComboBox<Integer> numCombo = new ComboBox<>();
        numCombo.getItems().addAll(3, 5, 8, 10);
        numCombo.setValue(5);
        numBox.getChildren().addAll(numLabel, numCombo);

        Button startBtn = new Button("Launch Live Interview Session →");
        stylePrimaryButton(startBtn);
        startBtn.setPrefHeight(45);
        startBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");

        startBtn.setOnAction(e -> {
            String selectedType = "MIXED";
            if (technicalRb.isSelected()) selectedType = "TECHNICAL";
            else if (hrRb.isSelected()) selectedType = "HR";

            String diff = diffCombo.getValue();
            int numQ = numCombo.getValue();

            String finalType = selectedType;
            Task<Interview> task = new Task<>() {
                @Override
                protected Interview call() throws Exception {
                    return interviewService.startInterview(student.getId(), finalType, diff, numQ);
                }
            };

            task.setOnSucceeded(ev -> {
                Interview interview = task.getValue();
                mainApp.showInterviewScreen(student, interview);
            });

            task.setOnFailed(ev -> Main.showErrorAlert("Launch Failed", task.getException().getMessage()));
            new Thread(task).start();
        });

        card.getChildren().addAll(title, desc, typeBox, diffBox, numBox, startBtn);
        container.getChildren().add(card);
        return container;
    }

    // =========================================================================
    // TAB 3: History
    // =========================================================================
    private Parent createHistoryTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Your Past Mock Interview Submissions");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh History");
        refreshBtn.setOnAction(e -> loadHistoryData());

        header.getChildren().addAll(title, spacer, refreshBtn);

        historyTable = new TableView<>();
        TableColumn<Result, String> idCol = new TableColumn<>("Result ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getResultId())));

        TableColumn<Result, String> intIdCol = new TableColumn<>("Interview ID");
        intIdCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getInterviewId())));

        TableColumn<Result, String> scoreCol = new TableColumn<>("Score Awarded");
        scoreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTotalScore() + " / " + data.getValue().getMaxScore()));

        TableColumn<Result, String> pctCol = new TableColumn<>("Percentage");
        pctCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPercentage() + "%"));

        TableColumn<Result, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade()));

        TableColumn<Result, String> dateCol = new TableColumn<>("Evaluated At");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEvaluatedAt() != null
                ? data.getValue().getEvaluatedAt().toString().replace('T', ' ').substring(0, 19) : "Just now"));

        historyTable.getColumns().addAll(idCol, intIdCol, scoreCol, pctCol, gradeCol, dateCol);
        loadHistoryData();

        container.getChildren().addAll(header, historyTable);
        return container;
    }

    private void loadHistoryData() {
        if (historyTable == null) return;
        Task<List<Result>> task = new Task<>() {
            @Override
            protected List<Result> call() throws Exception {
                return interviewService.getHistory(student.getId());
            }
        };
        task.setOnSucceeded(e -> historyTable.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> Main.showErrorAlert("History Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    // =========================================================================
    // TAB 4: Recommendations & Analytics
    // =========================================================================
    private Parent createRecommendationsTab() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(24));

        Label heading = new Label("Automated Skill Diagnostics & Growth Roadmap");
        heading.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));

        // Skill mastery bars / cards
        VBox scoresBox = new VBox(12);
        styleCard(scoresBox);
        scoresBox.setPadding(new Insets(20));

        Label scoresTitle = new Label("Domain Mastery Breakdown (Average Score)");
        scoresTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        scoresBox.getChildren().add(scoresTitle);

        VBox weakBox = new VBox(12);
        styleCard(weakBox);
        weakBox.setPadding(new Insets(20));
        weakBox.setStyle(weakBox.getStyle() + "; -fx-border-color: #fca5a5; -fx-background-color: #fff1f2;");

        Label weakTitle = new Label("Weak-Skill Detection (< 50% Threshold)");
        weakTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        weakTitle.setStyle("-fx-text-fill: #991b1b;");
        weakBox.getChildren().add(weakTitle);

        VBox recoBox = new VBox(12);
        styleCard(recoBox);
        recoBox.setPadding(new Insets(20));

        Label recoTitle = new Label("Personalized Remediation Recommendations");
        recoTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        recoBox.getChildren().add(recoTitle);

        // Load analytics in background
        Task<Void> task = new Task<>() {
            private Map<String, Double> skillScores;
            private List<String> weakSkills;
            private List<String> recommendations;

            @Override
            protected Void call() throws Exception {
                skillScores = analyticsService.getSkillScores(student.getId());
                weakSkills = analyticsService.getWeakSkills(student.getId());
                recommendations = analyticsService.getRecommendations(student.getId());
                return null;
            }

            @Override
            protected void succeeded() {
                for (Map.Entry<String, Double> entry : skillScores.entrySet()) {
                    ProgressBar bar = new ProgressBar(entry.getValue() / 100.0);
                    bar.setPrefWidth(300);
                    if (entry.getValue() < 50.0) {
                        bar.setStyle("-fx-accent: #ef4444;");
                    } else if (entry.getValue() >= 75.0) {
                        bar.setStyle("-fx-accent: #22c55e;");
                    } else {
                        bar.setStyle("-fx-accent: #3b82f6;");
                    }
                    Label name = new Label(entry.getKey() + ": " + entry.getValue() + "%");
                    name.setPrefWidth(180);
                    HBox row = new HBox(12, name, bar);
                    scoresBox.getChildren().add(row);
                }

                if (weakSkills.isEmpty()) {
                    Label allGood = new Label("✓ No weak skills detected! All competency averages exceed 50%.");
                    allGood.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
                    weakBox.getChildren().add(allGood);
                } else {
                    for (String w : weakSkills) {
                        Label item = new Label("⚠ Weak Domain: " + w + " — Immediate targeted revision suggested.");
                        item.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                        weakBox.getChildren().add(item);
                    }
                }

                for (String r : recommendations) {
                    Label item = new Label("• " + r);
                    item.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
                    recoBox.getChildren().add(item);
                }
            }
        };
        new Thread(task).start();

        container.getChildren().addAll(heading, scoresBox, weakBox, recoBox);
        scrollPane.setContent(container);
        return scrollPane;
    }

    // =========================================================================
    // TAB 5: Companies & Eligibility
    // =========================================================================
    private Parent createCompaniesTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Hiring Companies & Placement Eligibility Matrix");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Re-evaluate Eligibility");
        refreshBtn.setOnAction(e -> loadCompaniesAndRoles());

        header.getChildren().addAll(title, spacer, refreshBtn);

        companiesTable = new TableView<>();

        TableColumn<RoleRow, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().companyName));
        compCol.setPrefWidth(180);

        TableColumn<RoleRow, String> titleCol = new TableColumn<>("Job Role");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().roleTitle));
        titleCol.setPrefWidth(220);

        TableColumn<RoleRow, String> cgpaCol = new TableColumn<>("Min CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().minCgpa)));
        cgpaCol.setPrefWidth(90);

        TableColumn<RoleRow, String> skillsCol = new TableColumn<>("Required Skills");
        skillsCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().requiredSkillsStr));
        skillsCol.setPrefWidth(220);

        TableColumn<RoleRow, String> eligCol = new TableColumn<>("Eligibility Status");
        eligCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().eligibleStatus));
        eligCol.setPrefWidth(150);

        companiesTable.getColumns().addAll(compCol, titleCol, cgpaCol, skillsCol, eligCol);

        HBox actionBox = new HBox(16);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button applyBtn = new Button("Apply for Selected Role");
        stylePrimaryButton(applyBtn);

        applyBtn.setOnAction(e -> {
            RoleRow selected = companiesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Main.showErrorAlert("Select Role", "Please click on a job opening row to apply.");
                return;
            }

            Task<Void> applyTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    placementService.applyToRole(student.getId(), selected.roleId);
                    return null;
                }
            };
            applyTask.setOnSucceeded(ev -> {
                Main.showSuccessAlert("Application Sent", "Successfully submitted your application for '" + selected.roleTitle + "' at " + selected.companyName + "!");
                loadCompaniesAndRoles();
            });
            applyTask.setOnFailed(ev -> Main.showErrorAlert("Application Blocked", applyTask.getException().getMessage()));
            new Thread(applyTask).start();
        });

        actionBox.getChildren().addAll(applyBtn);
        loadCompaniesAndRoles();

        container.getChildren().addAll(header, companiesTable, actionBox);
        return container;
    }

    private void loadCompaniesAndRoles() {
        if (companiesTable == null) return;
        Task<List<RoleRow>> task = new Task<>() {
            @Override
            protected List<RoleRow> call() throws Exception {
                List<Company> companies = placementService.getAllCompanies();
                List<JobRole> eligibleRoles = placementService.getEligibleRoles(student.getId());
                List<RoleRow> rows = new java.util.ArrayList<>();

                for (Company c : companies) {
                    for (JobRole r : c.getJobRoles()) {
                        boolean isEligible = false;
                        for (JobRole er : eligibleRoles) {
                            if (er.getRoleId() == r.getRoleId()) {
                                isEligible = true;
                                break;
                            }
                        }

                        StringBuilder skillsStr = new StringBuilder();
                        for (Skill s : r.getRequiredSkills()) {
                            if (skillsStr.length() > 0) skillsStr.append(", ");
                            skillsStr.append(s.getSkillName());
                        }

                        String status;
                        if (student.hasAppliedToRole(r.getRoleId())) {
                            status = "✓ APPLIED";
                        } else if (isEligible) {
                            status = "★ ELIGIBLE";
                        } else {
                            if (student.getCgpa() < r.getMinCgpa()) {
                                status = "✗ CGPA < " + r.getMinCgpa();
                            } else {
                                status = "✗ Missing Skills";
                            }
                        }

                        rows.add(new RoleRow(r.getRoleId(), c.getName(), r.getTitle(), r.getMinCgpa(),
                                skillsStr.toString(), status));
                    }
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> companiesTable.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> Main.showErrorAlert("Companies Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    public static class RoleRow {
        public int roleId;
        public String companyName;
        public String roleTitle;
        public double minCgpa;
        public String requiredSkillsStr;
        public String eligibleStatus;

        public RoleRow(int roleId, String companyName, String roleTitle, double minCgpa,
                       String requiredSkillsStr, String eligibleStatus) {
            this.roleId = roleId;
            this.companyName = companyName;
            this.roleTitle = roleTitle;
            this.minCgpa = minCgpa;
            this.requiredSkillsStr = requiredSkillsStr;
            this.eligibleStatus = eligibleStatus;
        }
    }

    // ---------------- Styling Helpers ----------------

    private void styleCard(VBox card) {
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
    }

    private void stylePrimaryButton(Button btn) {
        btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18; -fx-cursor: hand;");
    }
}

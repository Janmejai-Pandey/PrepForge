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
import prepforge.model.Admin;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.TechnicalQuestion;
import prepforge.service.AnalyticsService;
import prepforge.service.InterviewService;
import prepforge.service.PlacementService;
import prepforge.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * UI Screen: AdminScreen
 * 
 * Contract Owner: Member 4 (UI & Integration)
 * 
 * Administration console for Placement Cell Officers containing all 4 contract tabs:
 * 1. Students Management
 * 2. Questions Bank Management
 * 3. Companies & Job Roles Posting
 * 4. Platform Statistics
 */
public class AdminScreen {

    private final Main mainApp;
    private final Admin admin;
    private final UserService userService;
    private final PlacementService placementService;
    private final InterviewService interviewService;
    private final AnalyticsService analyticsService;

    private TableView<Student> studentsTable;
    private TableView<Question> questionsTable;
    private TableView<CompanyRoleRow> postingsTable;

    public AdminScreen(Main mainApp, Admin admin, UserService userService,
                       PlacementService placementService, InterviewService interviewService,
                       AnalyticsService analyticsService) {
        this.mainApp = mainApp;
        this.admin = admin;
        this.userService = userService;
        this.placementService = placementService;
        this.interviewService = interviewService;
        this.analyticsService = analyticsService;
    }

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // ---------------- Top Bar ----------------
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 24, 16, 24));
        topBar.setStyle("-fx-background-color: #0f172a;");

        Label logo = new Label("PrepForge");
        logo.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        logo.setTextFill(Color.WHITE);

        Label badge = new Label("ADMIN CONSOLE");
        badge.setStyle("-fx-background-color: #e11d48; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label officer = new Label(admin.getName() + " (" + admin.getDepartment() + " | " + admin.getEmployeeCode() + ")");
        officer.setTextFill(Color.web("#cbd5e1"));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> mainApp.logout());

        topBar.getChildren().addAll(logo, badge, spacer, officer, logoutBtn);
        root.setTop(topBar);

        // ---------------- Center TabPane (4 Contract Tabs) ----------------
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab1 = new Tab("Students Management", createStudentsTab());
        Tab tab2 = new Tab("Question Bank", createQuestionsTab());
        Tab tab3 = new Tab("Companies & Job Roles", createCompaniesRolesTab());
        Tab tab4 = new Tab("Platform Statistics", createStatisticsTab());

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4);
        root.setCenter(tabPane);

        return root;
    }

    // =========================================================================
    // TAB 1: Students Management
    // =========================================================================
    private Parent createStudentsTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Registered Student Directory");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Directory");
        refreshBtn.setOnAction(e -> loadStudentsData());

        Button deleteBtn = new Button("Delete Student Account");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");

        deleteBtn.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Main.showErrorAlert("Selection Required", "Please select a student record to delete.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete student: " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            userService.deleteStudent(selected.getId());
                            return null;
                        }
                    };
                    task.setOnSucceeded(ev -> {
                        Main.showSuccessAlert("Deleted", "Student account deleted.");
                        loadStudentsData();
                    });
                    task.setOnFailed(ev -> Main.showErrorAlert("Delete Failed", task.getException().getMessage()));
                    new Thread(task).start();
                }
            });
        });

        header.getChildren().addAll(title, spacer, refreshBtn, deleteBtn);

        studentsTable = new TableView<>();

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(180);

        TableColumn<Student, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        emailCol.setPrefWidth(220);

        TableColumn<Student, String> enrollCol = new TableColumn<>("Enrollment");
        enrollCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEnrollmentNumber()));
        enrollCol.setPrefWidth(130);

        TableColumn<Student, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranch()));
        branchCol.setPrefWidth(180);

        TableColumn<Student, String> cgpaCol = new TableColumn<>("CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCgpa())));
        cgpaCol.setPrefWidth(80);

        TableColumn<Student, String> skillsCol = new TableColumn<>("Skills Enrolled");
        skillsCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSkills().size())));
        skillsCol.setPrefWidth(110);

        studentsTable.getColumns().addAll(idCol, nameCol, emailCol, enrollCol, branchCol, cgpaCol, skillsCol);
        loadStudentsData();

        container.getChildren().addAll(header, studentsTable);
        return container;
    }

    private void loadStudentsData() {
        if (studentsTable == null) return;
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                return userService.getAllStudents();
            }
        };
        task.setOnSucceeded(e -> studentsTable.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> Main.showErrorAlert("Load Error", task.getException().getMessage()));
        new Thread(task).start();
    }

    // =========================================================================
    // TAB 2: Questions Bank
    // =========================================================================
    private Parent createQuestionsTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));

        // Filter / Search bar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label searchLbl = new Label("Filter Questions:");
        searchLbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));

        TextField topicFilter = new TextField();
        topicFilter.setPromptText("Filter by Topic (e.g. Java, SQL)");

        ComboBox<String> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll("ALL", "EASY", "MEDIUM", "HARD");
        diffCombo.setValue("ALL");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("ALL", "MCQ", "TECHNICAL", "CODING", "HR");
        typeCombo.setValue("ALL");

        Button filterBtn = new Button("Apply Filter");
        filterBtn.setOnAction(e -> {
            String topic = topicFilter.getText().trim();
            String diff = diffCombo.getValue();
            String type = typeCombo.getValue();
            List<Question> filtered = interviewService.searchQuestions(topic, diff, type);
            questionsTable.setItems(FXCollections.observableArrayList(filtered));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addQBtn = new Button("+ Add New Question");
        stylePrimaryButton(addQBtn);
        addQBtn.setOnAction(e -> showAddQuestionDialog());

        Button deleteQBtn = new Button("Delete Question");
        deleteQBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 14; -fx-cursor: hand;");
        deleteQBtn.setOnAction(e -> {
            Question selected = questionsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Main.showErrorAlert("Select Question", "Please select a question in the table to delete.");
                return;
            }
            try {
                interviewService.deleteQuestion(selected.getId());
                loadQuestionsData();
                Main.showSuccessAlert("Deleted", "Question #" + selected.getId() + " deleted.");
            } catch (Exception ex) {
                Main.showErrorAlert("Delete Error", ex.getMessage());
            }
        });

        filterBar.getChildren().addAll(searchLbl, topicFilter, diffCombo, typeCombo, filterBtn, spacer, addQBtn, deleteQBtn);

        questionsTable = new TableView<>();

        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setPrefWidth(50);

        TableColumn<Question, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getQuestionType()));
        typeCol.setPrefWidth(100);

        TableColumn<Question, String> topicCol = new TableColumn<>("Topic");
        topicCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTopic()));
        topicCol.setPrefWidth(130);

        TableColumn<Question, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDifficulty()));
        diffCol.setPrefWidth(90);

        TableColumn<Question, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getMaxMarks())));
        marksCol.setPrefWidth(70);

        TableColumn<Question, String> textCol = new TableColumn<>("Question Prompt");
        textCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getText()));
        textCol.setPrefWidth(420);

        questionsTable.getColumns().addAll(idCol, typeCol, topicCol, diffCol, marksCol, textCol);
        loadQuestionsData();

        container.getChildren().addAll(filterBar, questionsTable);
        return container;
    }

    private void loadQuestionsData() {
        if (questionsTable == null) return;
        List<Question> all = interviewService.getAllQuestions();
        questionsTable.setItems(FXCollections.observableArrayList(all));
    }

    private void showAddQuestionDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Assessment Question");
        dialog.setHeaderText("Create a New Question for the Question Bank");

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("MCQ", "TECHNICAL", "CODING", "HR");
        typeBox.setValue("TECHNICAL");

        TextField topicTf = new TextField();
        topicTf.setPromptText("Topic (e.g. Java, SQL, Operating Systems)");

        ComboBox<String> diffBox = new ComboBox<>();
        diffBox.getItems().addAll("EASY", "MEDIUM", "HARD");
        diffBox.setValue("MEDIUM");

        TextField marksTf = new TextField("10.0");
        TextArea textTa = new TextArea();
        textTa.setPromptText("Enter complete question text...");
        textTa.setPrefRowCount(3);

        TextField keywordsTf = new TextField();
        keywordsTf.setPromptText("Comma-separated expected keywords or options (if MCQ)");

        form.getChildren().addAll(
                new Label("Question Type:"), typeBox,
                new Label("Topic:"), topicTf,
                new Label("Difficulty:"), diffBox,
                new Label("Marks:"), marksTf,
                new Label("Question Text:"), textTa,
                new Label("Keywords / Options:"), keywordsTf
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String qType = typeBox.getValue();
                    String topic = topicTf.getText().trim();
                    String diff = diffBox.getValue();
                    double marks = Double.parseDouble(marksTf.getText().trim());
                    String qText = textTa.getText().trim();

                    Question newQ;
                    if ("MCQ".equalsIgnoreCase(qType)) {
                        List<String> opts = Arrays.asList(keywordsTf.getText().split(","));
                        String correct = opts.isEmpty() ? "A" : opts.get(0).trim();
                        newQ = new MCQQuestion(qText, topic, diff, marks, opts, correct);
                    } else {
                        List<String> kws = Arrays.asList(keywordsTf.getText().split(","));
                        newQ = new TechnicalQuestion(qText, topic, diff, marks, kws);
                    }

                    interviewService.addQuestion(newQ);
                    loadQuestionsData();
                    Main.showSuccessAlert("Question Added", "Question successfully added to question bank.");
                } catch (Exception ex) {
                    Main.showErrorAlert("Add Question Failed", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // =========================================================================
    // TAB 3: Companies & Job Roles
    // =========================================================================
    private Parent createCompaniesRolesTab() {
        HBox container = new HBox(20);
        container.setPadding(new Insets(24));

        // Left: Forms
        VBox formsBox = new VBox(16);
        formsBox.setPrefWidth(380);

        // Form 1: Add Company
        VBox companyCard = new VBox(10);
        companyCard.setPadding(new Insets(16));
        styleCard(companyCard);

        Label cTitle = new Label("Register Recruiting Company");
        cTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 15));

        TextField cName = new TextField(); cName.setPromptText("Company Name (e.g. Amazon)");
        TextField cLoc = new TextField(); cLoc.setPromptText("Location (e.g. Bangalore)");
        TextField cInd = new TextField(); cInd.setPromptText("Industry (e.g. E-Commerce / Cloud)");

        Button saveCompBtn = new Button("Register Company");
        stylePrimaryButton(saveCompBtn);

        saveCompBtn.setOnAction(e -> {
            try {
                Company c = new Company(cName.getText().trim(), cLoc.getText().trim(), cInd.getText().trim());
                placementService.saveCompany(c);
                Main.showSuccessAlert("Company Saved", "Company '" + c.getName() + "' registered!");
                cName.clear(); cLoc.clear(); cInd.clear();
                loadCompaniesRolesTable();
            } catch (Exception ex) {
                Main.showErrorAlert("Save Failed", ex.getMessage());
            }
        });

        companyCard.getChildren().addAll(cTitle, cName, cLoc, cInd, saveCompBtn);

        // Form 2: Post Job Role
        VBox roleCard = new VBox(10);
        roleCard.setPadding(new Insets(16));
        styleCard(roleCard);

        Label rTitle = new Label("Post Campus Job Opening");
        rTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 15));

        ComboBox<Company> compDropdown = new ComboBox<>();
        compDropdown.setPromptText("Select Hiring Company");
        try {
            compDropdown.setItems(FXCollections.observableArrayList(placementService.getAllCompanies()));
        } catch (Exception ex) {}

        TextField roleTitleTf = new TextField(); roleTitleTf.setPromptText("Job Title (e.g. Software Engineer)");
        TextField minCgpaTf = new TextField("7.5"); minCgpaTf.setPromptText("Minimum CGPA Cutoff");

        Button postRoleBtn = new Button("Publish Job Opening");
        stylePrimaryButton(postRoleBtn);

        postRoleBtn.setOnAction(e -> {
            try {
                Company comp = compDropdown.getValue();
                if (comp == null) {
                    Main.showErrorAlert("Company Missing", "Please select a company.");
                    return;
                }
                String title = roleTitleTf.getText().trim();
                double minCgpa = Double.parseDouble(minCgpaTf.getText().trim());

                JobRole role = new JobRole(title, minCgpa, comp);
                // Assign standard core skills
                role.addRequiredSkill(new Skill(1, "Java", "Programming"));
                role.addRequiredSkill(new Skill(3, "SQL", "Database"));

                placementService.saveJobRole(role);
                Main.showSuccessAlert("Role Posted", "Job opening '" + title + "' published successfully!");
                roleTitleTf.clear();
                loadCompaniesRolesTable();
            } catch (Exception ex) {
                Main.showErrorAlert("Posting Failed", ex.getMessage());
            }
        });

        roleCard.getChildren().addAll(rTitle, compDropdown, roleTitleTf, minCgpaTf, postRoleBtn);
        formsBox.getChildren().addAll(companyCard, roleCard);

        // Right: Current Postings Table
        VBox tableBox = new VBox(12);
        HBox.setHgrow(tableBox, Priority.ALWAYS);
        tableBox.setPadding(new Insets(16));
        styleCard(tableBox);

        Label tableTitle = new Label("Active Placement Drives & Openings");
        tableTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));

        postingsTable = new TableView<>();
        TableColumn<CompanyRoleRow, String> cCol = new TableColumn<>("Company");
        cCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().company));
        cCol.setPrefWidth(180);

        TableColumn<CompanyRoleRow, String> rCol = new TableColumn<>("Job Role");
        rCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().role));
        rCol.setPrefWidth(220);

        TableColumn<CompanyRoleRow, String> gCol = new TableColumn<>("Min CGPA");
        gCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().minCgpa)));
        gCol.setPrefWidth(90);

        TableColumn<CompanyRoleRow, String> locCol = new TableColumn<>("Location / Sector");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().location));
        locCol.setPrefWidth(200);

        postingsTable.getColumns().addAll(cCol, rCol, gCol, locCol);
        loadCompaniesRolesTable();

        tableBox.getChildren().addAll(tableTitle, postingsTable);
        container.getChildren().addAll(formsBox, tableBox);
        return container;
    }

    private void loadCompaniesRolesTable() {
        if (postingsTable == null) return;
        Task<List<CompanyRoleRow>> task = new Task<>() {
            @Override
            protected List<CompanyRoleRow> call() throws Exception {
                List<Company> companies = placementService.getAllCompanies();
                List<CompanyRoleRow> rows = new java.util.ArrayList<>();
                for (Company c : companies) {
                    for (JobRole r : c.getJobRoles()) {
                        rows.add(new CompanyRoleRow(c.getName(), r.getTitle(), r.getMinCgpa(), c.getLocation() + " (" + c.getIndustry() + ")"));
                    }
                }
                return rows;
            }
        };
        task.setOnSucceeded(e -> postingsTable.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    public static class CompanyRoleRow {
        public String company;
        public String role;
        public double minCgpa;
        public String location;
        public CompanyRoleRow(String company, String role, double minCgpa, String location) {
            this.company = company; this.role = role; this.minCgpa = minCgpa; this.location = location;
        }
    }

    // =========================================================================
    // TAB 4: Platform Statistics
    // =========================================================================
    private Parent createStatisticsTab() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(30));

        Label heading = new Label("Institutional Placement Intelligence & Platform Statistics");
        heading.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));

        FlowPane statsCards = new FlowPane(20, 20);

        Task<Map<String, Integer>> task = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                return analyticsService.getPlatformStats();
            }

            @Override
            protected void succeeded() {
                Map<String, Integer> stats = getValue();
                String[] colors = {"#2563eb", "#16a34a", "#9333ea", "#d97706", "#0891b2"};
                int colorIdx = 0;

                for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                    VBox statCard = new VBox(10);
                    statCard.setPrefSize(220, 120);
                    statCard.setPadding(new Insets(16));
                    statCard.setAlignment(Pos.CENTER_LEFT);
                    statCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);");

                    Label valLabel = new Label(String.valueOf(entry.getValue()));
                    valLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 28));
                    valLabel.setStyle("-fx-text-fill: " + colors[colorIdx % colors.length] + ";");

                    Label keyLabel = new Label(entry.getKey());
                    keyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: bold;");
                    keyLabel.setWrapText(true);

                    statCard.getChildren().addAll(valLabel, keyLabel);
                    statsCards.getChildren().add(statCard);
                    colorIdx++;
                }
            }
        };
        new Thread(task).start();

        container.getChildren().addAll(heading, statsCards);
        return container;
    }

    // ---------------- Styling Helpers ----------------

    private void styleCard(VBox card) {
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
    }

    private void stylePrimaryButton(Button btn) {
        btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18; -fx-cursor: hand;");
    }
}

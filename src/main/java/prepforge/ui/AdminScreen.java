package prepforge.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import prepforge.Main;
import prepforge.model.Admin;
import prepforge.model.ApplicationRecord;
import prepforge.model.CodingQuestion;
import prepforge.model.Company;
import prepforge.model.HRQuestion;
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

public class AdminScreen {

    private final Main mainApp;
    private final Admin admin;
    private final UserService userService;
    private final PlacementService placementService;
    private final InterviewService interviewService;
    private final AnalyticsService analyticsService;

    private TableView<Student> studentsTable;
    private TableView<ApplicationRecord> applicationsTable;
    private TableView<Question> questionsTable;
    private TableView<CompanyRoleRow> postingsTable;
    private Label statStudents;
    private Label statInterviews;
    private Label statResults;
    private Label statQuestions;
    private Label statCompanies;
    private Label statRoles;

    public AdminScreen(Main mainApp, Admin admin, UserService userService, PlacementService placementService,
                       InterviewService interviewService, AnalyticsService analyticsService) {
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

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(16, 28, 16, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label brand = new Label("PrepForge");
        brand.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));
        brand.setTextFill(Color.web("#1e40af"));

        Label separator = new Label("|");
        separator.setTextFill(Color.web("#cbd5e1"));

        Label welcome = new Label("Admin Console — " + admin.getName() + " (" + admin.getDepartment() + ")");
        welcome.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 14));
        welcome.setTextFill(Color.web("#334155"));

        Label adminBadge = new Label("ADMINISTRATOR");
        adminBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> mainApp.logout());

        topBar.getChildren().addAll(brand, separator, welcome, adminBadge, spacer, logoutBtn);
        root.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab studentsTab = new Tab("Students Management", buildStudentsTab());
        Tab questionsTab = new Tab("Question Bank", buildQuestionBankTab());
        Tab companiesTab = new Tab("Companies & Job Roles", buildCompaniesAndRolesTab());
        Tab applicationsTab = new Tab("Applications & Shortlisting", buildApplicationsTab());
        Tab statsTab = new Tab("Platform Statistics", buildPlatformStatisticsTab());

        tabPane.getTabs().addAll(studentsTab, questionsTab, companiesTab, applicationsTab, statsTab);
        root.setCenter(tabPane);

        loadStudentsData();
        loadQuestionsData(null, null, null);
        loadPostingsData();
        loadApplicationsData();
        loadPlatformStatsData();

        return root;
    }

    private Parent buildStudentsTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: white;");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Registered Candidate Roster");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Roster");
        refreshBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadStudentsData());

        Button deleteBtn = new Button("Delete Selected Candidate");
        deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        deleteBtn.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a student from the table.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.deleteStudent(selected.getId());
                    return null;
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Success", "Student record deleted successfully.");
                loadStudentsData();
                loadPlatformStatsData();
            });

            task.setOnFailed(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Deletion Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        Button exportCsvBtn = new Button("Export Roster to CSV");
        exportCsvBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        exportCsvBtn.setOnAction(e -> exportStudentsToCsv());

        header.getChildren().addAll(title, spacer, refreshBtn, exportCsvBtn, deleteBtn);

        studentsTable = new TableView<>();
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<Student, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<Student, String> enrollCol = new TableColumn<>("Enrollment No");
        enrollCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEnrollmentNumber()));

        TableColumn<Student, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranch()));

        TableColumn<Student, String> cgpaCol = new TableColumn<>("CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getCgpa())));

        studentsTable.getColumns().addAll(idCol, nameCol, emailCol, enrollCol, branchCol, cgpaCol);
        VBox.setVgrow(studentsTable, Priority.ALWAYS);

        container.getChildren().addAll(header, studentsTable);
        return container;
    }

    private Parent buildQuestionBankTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: white;");

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField topicFilter = new TextField();
        topicFilter.setPromptText("Filter by topic...");

        ComboBox<String> diffFilter = new ComboBox<>();
        diffFilter.getItems().addAll("All Difficulties", "EASY", "MEDIUM", "HARD");
        diffFilter.setValue("All Difficulties");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "MCQ", "TECHNICAL", "CODING", "HR");
        typeFilter.setValue("All Types");

        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        searchBtn.setOnAction(e -> {
            String topic = topicFilter.getText().trim();
            String diff = "All Difficulties".equals(diffFilter.getValue()) ? null : diffFilter.getValue();
            String type = "All Types".equals(typeFilter.getValue()) ? null : typeFilter.getValue();
            loadQuestionsData(topic.isEmpty() ? null : topic, diff, type);
        });

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        Button deleteQBtn = new Button("Delete Selected Question");
        deleteQBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        deleteQBtn.setOnAction(e -> {
            Question selected = questionsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a question from the table.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    interviewService.deleteQuestion(selected.getId());
                    return null;
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Success", "Question deleted from repository.");
                loadQuestionsData(null, null, null);
                loadPlatformStatsData();
            });

            task.setOnFailed(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Delete Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        filterBar.getChildren().addAll(new Label("Topic:"), topicFilter, new Label("Difficulty:"), diffFilter,
                new Label("Type:"), typeFilter, searchBtn, filterSpacer, deleteQBtn);

        questionsTable = new TableView<>();
        questionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Question, String> qIdCol = new TableColumn<>("ID");
        qIdCol.setMaxWidth(60);
        qIdCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<Question, String> qTypeCol = new TableColumn<>("Type");
        qTypeCol.setMaxWidth(100);
        qTypeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));

        TableColumn<Question, String> qTopicCol = new TableColumn<>("Topic");
        qTopicCol.setMaxWidth(130);
        qTopicCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTopic()));

        TableColumn<Question, String> qDiffCol = new TableColumn<>("Difficulty");
        qDiffCol.setMaxWidth(100);
        qDiffCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDifficulty()));

        TableColumn<Question, String> qTextCol = new TableColumn<>("Question Statement");
        qTextCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getText()));

        TableColumn<Question, String> qMarksCol = new TableColumn<>("Max Marks");
        qMarksCol.setMaxWidth(90);
        qMarksCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getMaxMarks())));

        questionsTable.getColumns().addAll(qIdCol, qTypeCol, qTopicCol, qDiffCol, qTextCol, qMarksCol);
        VBox.setVgrow(questionsTable, Priority.ALWAYS);

        VBox addForm = new VBox(10);
        addForm.setPadding(new Insets(14));
        addForm.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Label formTitle = new Label("Add New Question to Bank");
        formTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));

        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> newType = new ComboBox<>();
        newType.getItems().addAll("MCQ", "TECHNICAL", "CODING", "HR");
        newType.setValue("MCQ");

        TextField newTopic = new TextField();
        newTopic.setPromptText("Topic (e.g. Java, DBMS, SQL)");

        ComboBox<String> newDiff = new ComboBox<>();
        newDiff.getItems().addAll("EASY", "MEDIUM", "HARD");
        newDiff.setValue("EASY");

        row1.getChildren().addAll(new Label("Type:"), newType, new Label("Topic:"), newTopic, new Label("Difficulty:"), newDiff);

        TextField newText = new TextField();
        newText.setPromptText("Question Text / Prompt...");

        TextField extraField1 = new TextField();
        extraField1.setPromptText("Options comma-separated: Option A, Option B, Option C, Option D");

        TextField extraField2 = new TextField();
        extraField2.setPromptText("Correct Option Index (0 for first, 1 for second, etc.)");

        newType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("MCQ".equals(newVal)) {
                extraField1.setVisible(true);
                extraField1.setManaged(true);
                extraField1.setPromptText("Options comma-separated: Option A, Option B, Option C, Option D");
                extraField2.setVisible(true);
                extraField2.setManaged(true);
                extraField2.setPromptText("Correct Option Index (0, 1, 2, 3)");
            } else if ("CODING".equals(newVal)) {
                extraField1.setVisible(true);
                extraField1.setManaged(true);
                extraField1.setPromptText("Problem Statement / Technical Constraints");
                extraField2.setVisible(true);
                extraField2.setManaged(true);
                extraField2.setPromptText("Expected keywords comma-separated (e.g. hashmap, node, null)");
            } else {
                extraField1.setVisible(true);
                extraField1.setManaged(true);
                extraField1.setPromptText("Expected keywords comma-separated (e.g. encapsulation, polymorphism)");
                extraField2.setVisible(false);
                extraField2.setManaged(false);
            }
        });

        Button addQBtn = new Button("Add Question");
        addQBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;");

        addQBtn.setOnAction(e -> {
            String ty = newType.getValue();
            String top = newTopic.getText().trim();
            String dif = newDiff.getValue();
            String txt = newText.getText().trim();

            if (top.isEmpty() || txt.isEmpty()) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Validation", "Topic and Question text cannot be empty.");
                return;
            }

            try {
                Question q;
                if ("MCQ".equals(ty)) {
                    String[] opts = extraField1.getText().split(",");
                    List<String> optList = new ArrayList<>();
                    for (String o : opts) {
                        if (!o.trim().isEmpty()) optList.add(o.trim());
                    }
                    int cIdx = Integer.parseInt(extraField2.getText().trim());
                    q = new MCQQuestion(txt, top, dif, optList, cIdx);
                } else if ("CODING".equals(ty)) {
                    String prob = extraField1.getText().trim();
                    String[] kws = extraField2.getText().split(",");
                    List<String> kwList = new ArrayList<>();
                    for (String k : kws) {
                        if (!k.trim().isEmpty()) kwList.add(k.trim());
                    }
                    q = new CodingQuestion(txt, top, dif, prob, kwList);
                } else if ("HR".equals(ty)) {
                    String[] kws = extraField1.getText().split(",");
                    List<String> kwList = new ArrayList<>();
                    for (String k : kws) {
                        if (!k.trim().isEmpty()) kwList.add(k.trim());
                    }
                    q = new HRQuestion(txt, top, dif, kwList);
                } else {
                    String[] kws = extraField1.getText().split(",");
                    List<String> kwList = new ArrayList<>();
                    for (String k : kws) {
                        if (!k.trim().isEmpty()) kwList.add(k.trim());
                    }
                    q = new TechnicalQuestion(txt, top, dif, kwList);
                }

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        interviewService.addQuestion(q);
                        return null;
                    }
                };

                task.setOnSucceeded(workerStateEvent -> {
                    mainApp.showAlert(Alert.AlertType.INFORMATION, "Success", "Question successfully added to repository.");
                    newText.clear();
                    extraField1.clear();
                    extraField2.clear();
                    loadQuestionsData(null, null, null);
                    loadPlatformStatsData();
                });

                task.setOnFailed(workerStateEvent -> {
                    mainApp.showAlert(Alert.AlertType.ERROR, "Add Failed", task.getException().getMessage());
                });

                new Thread(task).start();
            } catch (Exception ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Input Error", ex.getMessage());
            }
        });

        addForm.getChildren().addAll(formTitle, row1, newText, extraField1, extraField2, addQBtn);

        container.getChildren().addAll(filterBar, questionsTable, addForm);
        return container;
    }

    private Parent buildCompaniesAndRolesTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: white;");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Placement Drives & Vacancy Management");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Postings");
        refreshBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadPostingsData());

        header.getChildren().addAll(title, spacer, refreshBtn);

        postingsTable = new TableView<>();
        postingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CompanyRoleRow, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));

        TableColumn<CompanyRoleRow, String> sectorCol = new TableColumn<>("Industry");
        sectorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIndustry()));

        TableColumn<CompanyRoleRow, String> roleCol = new TableColumn<>("Job Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoleTitle()));

        TableColumn<CompanyRoleRow, String> minCgpaCol = new TableColumn<>("Cutoff CGPA");
        minCgpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getMinCgpa())));

        TableColumn<CompanyRoleRow, String> skillsCol = new TableColumn<>("Required Skills");
        skillsCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequiredSkillsStr()));

        postingsTable.getColumns().addAll(compCol, sectorCol, roleCol, minCgpaCol, skillsCol);
        VBox.setVgrow(postingsTable, Priority.ALWAYS);

        HBox formsContainer = new HBox(24);

        VBox addCompanyCard = new VBox(10);
        addCompanyCard.setPrefWidth(420);
        addCompanyCard.setPadding(new Insets(16));
        addCompanyCard.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

        Label compHeader = new Label("Add Recruiting Company");
        compHeader.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));

        TextField compName = new TextField();
        compName.setPromptText("Company Name (e.g. Oracle)");
        TextField compSector = new TextField();
        compSector.setPromptText("Industry / Sector (e.g. Cloud Database)");
        TextField compLoc = new TextField();
        compLoc.setPromptText("Location (e.g. Bengaluru, India)");

        Button saveCompBtn = new Button("Register Company");
        saveCompBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        saveCompBtn.setOnAction(e -> {
            String name = compName.getText().trim();
            String sector = compSector.getText().trim();
            String loc = compLoc.getText().trim();

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Company c = new Company(name, loc, sector);
                    placementService.saveCompany(c);
                    return null;
                }
            };

            task.setOnSucceeded(ev -> {
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Success", "Company registered successfully.");
                compName.clear();
                compSector.clear();
                compLoc.clear();
                loadPostingsData();
                loadPlatformStatsData();
            });

            task.setOnFailed(ev -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        addCompanyCard.getChildren().addAll(compHeader, compName, compSector, compLoc, saveCompBtn);

        VBox addRoleCard = new VBox(10);
        HBox.setHgrow(addRoleCard, Priority.ALWAYS);
        addRoleCard.setPadding(new Insets(16));
        addRoleCard.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

        Label roleHeader = new Label("Post New Job Role");
        roleHeader.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));

        ComboBox<Company> compDropdown = new ComboBox<>();
        compDropdown.setPromptText("Select Hiring Company");
        compDropdown.setMaxWidth(Double.MAX_VALUE);

        TextField roleTitle = new TextField();
        roleTitle.setPromptText("Role Title (e.g. SDE-1 Backend)");

        TextField minCgpa = new TextField();
        minCgpa.setPromptText("Minimum CGPA Cutoff (0.00 - 10.00)");

        Button saveRoleBtn = new Button("Publish Job Role");
        saveRoleBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        saveRoleBtn.setOnAction(e -> {
            Company c = compDropdown.getValue();
            if (c == null) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Validation", "Please select a company.");
                return;
            }

            String titleStr = roleTitle.getText().trim();
            double cgpaVal;
            try {
                cgpaVal = Double.parseDouble(minCgpa.getText().trim());
            } catch (NumberFormatException ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Validation", "CGPA must be a valid decimal number.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    JobRole role = new JobRole(titleStr, cgpaVal, c);
                    placementService.saveJobRole(role);
                    return null;
                }
            };

            task.setOnSucceeded(ev -> {
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Success", "Job role published successfully.");
                roleTitle.clear();
                minCgpa.clear();
                loadPostingsData();
                loadPlatformStatsData();
            });

            task.setOnFailed(ev -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        addRoleCard.getChildren().addAll(roleHeader, compDropdown, roleTitle, minCgpa, saveRoleBtn);
        formsContainer.getChildren().addAll(addCompanyCard, addRoleCard);

        container.getChildren().addAll(header, postingsTable, formsContainer);
        return container;
    }

    private Parent buildPlatformStatisticsTab() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white;");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("PrepForge Platform Analytics & Placement Health");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Platform Metrics");
        refreshBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadPlatformStatsData());

        header.getChildren().addAll(title, spacer, refreshBtn);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        statStudents = createStatValueLabel();
        statInterviews = createStatValueLabel();
        statResults = createStatValueLabel();
        statQuestions = createStatValueLabel();
        statCompanies = createStatValueLabel();
        statRoles = createStatValueLabel();

        VBox cardStudents = createStatCard("Registered Students", statStudents, "#2563eb", "#eff6ff");
        VBox cardInterviews = createStatCard("Interviews Conducted", statInterviews, "#059669", "#ecfdf5");
        VBox cardResults = createStatCard("Evaluated Submissions", statResults, "#7c3aed", "#f5f3ff");
        VBox cardQuestions = createStatCard("Questions in Bank", statQuestions, "#d97706", "#fffbeb");
        VBox cardCompanies = createStatCard("Recruiting Partners", statCompanies, "#0891b2", "#ecfeff");
        VBox cardRoles = createStatCard("Active Placement Drives", statRoles, "#db2777", "#fdf2f8");

        grid.add(cardStudents, 0, 0);
        grid.add(cardInterviews, 1, 0);
        grid.add(cardResults, 2, 0);
        grid.add(cardQuestions, 0, 1);
        grid.add(cardCompanies, 1, 1);
        grid.add(cardRoles, 2, 1);

        container.getChildren().addAll(header, grid);
        return container;
    }

    private Label createStatValueLabel() {
        Label lbl = new Label("0");
        lbl.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
        return lbl;
    }

    private VBox createStatCard(String labelText, Label valueLabel, String accentColor, String bgColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 8; -fx-background-radius: 8;", bgColor, accentColor));

        Label title = new Label(labelText);
        title.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 14));
        title.setTextFill(Color.web("#475569"));

        valueLabel.setTextFill(Color.web(accentColor));

        card.getChildren().addAll(title, valueLabel);
        return card;
    }

    private void loadStudentsData() {
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                return userService.getAllStudents();
            }
        };

        task.setOnSucceeded(e -> {
            if (studentsTable != null) {
                studentsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            }
        });

        new Thread(task).start();
    }

    private void loadQuestionsData(String topic, String difficulty, String type) {
        Task<List<Question>> task = new Task<>() {
            @Override
            protected List<Question> call() throws Exception {
                return interviewService.searchQuestions(topic, difficulty, type);
            }
        };

        task.setOnSucceeded(e -> {
            if (questionsTable != null) {
                questionsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            }
        });

        new Thread(task).start();
    }

    private void loadPostingsData() {
        Task<List<CompanyRoleRow>> task = new Task<>() {
            @Override
            protected List<CompanyRoleRow> call() throws Exception {
                List<Company> companies = placementService.getAllCompanies();
                List<CompanyRoleRow> rows = new ArrayList<>();
                for (Company c : companies) {
                    for (JobRole r : c.getJobRoles()) {
                        StringBuilder sb = new StringBuilder();
                        for (Skill sk : r.getRequiredSkills()) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(sk.getName());
                        }
                        rows.add(new CompanyRoleRow(c.getName(), c.getIndustry(), r.getTitle(), r.getMinCgpa(), sb.toString()));
                    }
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> {
            if (postingsTable != null) {
                postingsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            }
        });

        new Thread(task).start();
    }

    private void loadPlatformStatsData() {
        Task<PlatformMetrics> task = new Task<>() {
            @Override
            protected PlatformMetrics call() throws Exception {
                Map<String, Integer> daoStats = analyticsService.getPlatformStats();
                int studentCount = userService.getAllStudents().size();
                int questionCount = interviewService.searchQuestions(null, null, null).size();
                List<Company> comps = placementService.getAllCompanies();
                int compCount = comps.size();
                int roleCount = 0;
                for (Company c : comps) {
                    roleCount += c.getJobRoles().size();
                }
                int interviews = daoStats.getOrDefault("totalInterviews", 0);
                int results = daoStats.getOrDefault("totalResults", 0);

                return new PlatformMetrics(studentCount, interviews, results, questionCount, compCount, roleCount);
            }
        };

        task.setOnSucceeded(e -> {
            PlatformMetrics m = task.getValue();
            if (statStudents != null) statStudents.setText(String.valueOf(m.students));
            if (statInterviews != null) statInterviews.setText(String.valueOf(m.interviews));
            if (statResults != null) statResults.setText(String.valueOf(m.results));
            if (statQuestions != null) statQuestions.setText(String.valueOf(m.questions));
            if (statCompanies != null) statCompanies.setText(String.valueOf(m.companies));
            if (statRoles != null) statRoles.setText(String.valueOf(m.roles));
        });

        new Thread(task).start();
    }

    private static class PlatformMetrics {
        final int students;
        final int interviews;
        final int results;
        final int questions;
        final int companies;
        final int roles;

        PlatformMetrics(int students, int interviews, int results, int questions, int companies, int roles) {
            this.students = students;
            this.interviews = interviews;
            this.results = results;
            this.questions = questions;
            this.companies = companies;
            this.roles = roles;
        }
    }

    public static class CompanyRoleRow {
        private final String companyName;
        private final String industry;
        private final String roleTitle;
        private final double minCgpa;
        private final String requiredSkillsStr;

        public CompanyRoleRow(String companyName, String industry, String roleTitle, double minCgpa, String requiredSkillsStr) {
            this.companyName = companyName;
            this.industry = industry;
            this.roleTitle = roleTitle;
            this.minCgpa = minCgpa;
            this.requiredSkillsStr = requiredSkillsStr;
        }

        public String getCompanyName() { return companyName; }
        public String getIndustry() { return industry; }
        public String getRoleTitle() { return roleTitle; }
        public double getMinCgpa() { return minCgpa; }
        public String getRequiredSkillsStr() { return requiredSkillsStr; }
    }

    private void exportStudentsToCsv() {
        if (studentsTable == null || studentsTable.getItems().isEmpty()) {
            mainApp.showAlert(Alert.AlertType.WARNING, "No Data", "No student records available to export.");
            return;
        }
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Student Roster to CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("PrepForge_Student_Roster.csv");
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                writer.println("Student ID,Full Name,Email,Enrollment No,Branch,CGPA");
                for (Student s : studentsTable.getItems()) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.2f%n",
                            s.getId(),
                            s.getName().replace("\"", "\"\""),
                            s.getEmail().replace("\"", "\"\""),
                            s.getEnrollmentNumber().replace("\"", "\"\""),
                            s.getBranch().replace("\"", "\"\""),
                            s.getCgpa());
                }
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Candidate roster exported to " + file.getName());
            } catch (java.io.IOException ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to write CSV: " + ex.getMessage());
            }
        }
    }

    private Parent buildApplicationsTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: white;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Placement Applications & Candidate Shortlisting");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadApplicationsData());

        Button shortlistBtn = new Button("Shortlist");
        shortlistBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        shortlistBtn.setOnAction(e -> updateSelectedAppStatus("SHORTLISTED"));

        Button selectBtn = new Button("Mark Selected");
        selectBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        selectBtn.setOnAction(e -> updateSelectedAppStatus("SELECTED"));

        Button rejectBtn = new Button("Reject");
        rejectBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        rejectBtn.setOnAction(e -> updateSelectedAppStatus("REJECTED"));

        Button exportBtn = new Button("Export CSV");
        exportBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> exportApplicationsToCsv());

        header.getChildren().addAll(title, spacer, refreshBtn, shortlistBtn, selectBtn, rejectBtn, exportBtn);

        applicationsTable = new TableView<>();
        applicationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ApplicationRecord, String> idCol = new TableColumn<>("App ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getApplicationId())));

        TableColumn<ApplicationRecord, String> nameCol = new TableColumn<>("Candidate Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));

        TableColumn<ApplicationRecord, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentEmail()));

        TableColumn<ApplicationRecord, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBranch()));

        TableColumn<ApplicationRecord, String> cgpaCol = new TableColumn<>("CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getCgpa())));

        TableColumn<ApplicationRecord, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));

        TableColumn<ApplicationRecord, String> roleCol = new TableColumn<>("Role Title");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoleTitle()));

        TableColumn<ApplicationRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<ApplicationRecord, String> dateCol = new TableColumn<>("Applied Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAppliedAt() != null ? d.getValue().getAppliedAt().toString().replace('T', ' ').substring(0, 16) : ""));

        applicationsTable.getColumns().addAll(idCol, nameCol, emailCol, branchCol, cgpaCol, companyCol, roleCol, statusCol, dateCol);
        VBox.setVgrow(applicationsTable, Priority.ALWAYS);

        container.getChildren().addAll(header, applicationsTable);
        return container;
    }

    private void loadApplicationsData() {
        Task<List<ApplicationRecord>> task = new Task<>() {
            @Override
            protected List<ApplicationRecord> call() throws Exception {
                return placementService.getAllApplications();
            }
        };

        task.setOnSucceeded(e -> {
            if (applicationsTable != null) {
                applicationsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            }
        });

        new Thread(task).start();
    }

    private void updateSelectedAppStatus(String newStatus) {
        ApplicationRecord selected = applicationsTable != null ? applicationsTable.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            mainApp.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an application record from the table.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                placementService.updateApplicationStatus(selected.getStudentId(), selected.getRoleId(), newStatus);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            mainApp.showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Application #" + selected.getApplicationId() + " marked as " + newStatus);
            loadApplicationsData();
        });

        task.setOnFailed(e -> {
            mainApp.showAlert(Alert.AlertType.ERROR, "Update Failed", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void exportApplicationsToCsv() {
        if (applicationsTable == null || applicationsTable.getItems().isEmpty()) {
            mainApp.showAlert(Alert.AlertType.WARNING, "No Data", "No application records available to export.");
            return;
        }
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Applications to CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("PrepForge_Applications.csv");
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                writer.println("Application ID,Candidate Name,Email,Branch,CGPA,Company,Role,Status,Applied At");
                for (ApplicationRecord a : applicationsTable.getItems()) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                            a.getApplicationId(),
                            a.getStudentName().replace("\"", "\"\""),
                            a.getStudentEmail().replace("\"", "\"\""),
                            a.getBranch().replace("\"", "\"\""),
                            a.getCgpa(),
                            a.getCompanyName().replace("\"", "\"\""),
                            a.getRoleTitle().replace("\"", "\"\""),
                            a.getStatus(),
                            a.getAppliedAt() != null ? a.getAppliedAt().toString() : "");
                }
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Applications exported to " + file.getName());
            } catch (java.io.IOException ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to write CSV: " + ex.getMessage());
            }
        }
    }
}

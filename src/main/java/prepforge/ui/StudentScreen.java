package prepforge.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import prepforge.Main;
import prepforge.exception.PrepForgeException;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.service.PlacementService;
import prepforge.service.UserService;

public class StudentScreen {

    private final Main mainApp;
    private final Student student;
    private final UserService userService;
    private final PlacementService placementService;

    private TableView<SkillRow> skillsTable;
    private TableView<RoleRow> companiesTable;
    private Label cgpaBadge;

    public StudentScreen(Main mainApp, Student student, UserService userService, PlacementService placementService) {
        this.mainApp = mainApp;
        this.student = student;
        this.userService = userService;
        this.placementService = placementService;
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

        Label welcome = new Label("Student Portal — " + student.getName() + " (" + student.getBranch() + ")");
        welcome.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 14));
        welcome.setTextFill(Color.web("#334155"));

        cgpaBadge = new Label("CGPA: " + String.format("%.2f", student.getCgpa()));
        cgpaBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> mainApp.logout());

        topBar.getChildren().addAll(brand, separator, welcome, cgpaBadge, spacer, logoutBtn);
        root.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab profileTab = new Tab("Profile & Skills", buildProfileAndSkillsTab());
        Tab placementTab = new Tab("Companies & Placement Eligibility", buildCompaniesTab());

        tabPane.getTabs().addAll(profileTab, placementTab);
        root.setCenter(tabPane);

        loadSkillsData();
        loadCompaniesData();

        return root;
    }

    private Parent buildProfileAndSkillsTab() {
        HBox container = new HBox(24);
        container.setPadding(new Insets(24));

        VBox profileCard = new VBox(16);
        profileCard.setPrefWidth(380);
        profileCard.setPadding(new Insets(20));
        profileCard.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label cardTitle = new Label("Student Academic Profile");
        cardTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        cardTitle.setTextFill(Color.web("#0f172a"));

        TextField nameField = new TextField(student.getName());
        TextField emailField = new TextField(student.getEmail());
        emailField.setDisable(true);
        TextField enrollField = new TextField(student.getEnrollmentNumber());
        enrollField.setDisable(true);
        TextField branchField = new TextField(student.getBranch());
        TextField cgpaField = new TextField(String.valueOf(student.getCgpa()));

        Button updateBtn = new Button("Save Profile Changes");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 0;");

        updateBtn.setOnAction(e -> {
            try {
                double newCgpa = Double.parseDouble(cgpaField.getText().trim());
                student.setName(nameField.getText().trim());
                student.setBranch(branchField.getText().trim());
                student.setCgpa(newCgpa);

                userService.updateProfile(student);
                cgpaBadge.setText("CGPA: " + String.format("%.2f", newCgpa));
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your profile details have been saved.");
                loadCompaniesData();
            } catch (NumberFormatException ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Invalid Input", "CGPA must be a valid numeric decimal (e.g. 8.5).");
            } catch (PrepForgeException ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Update Failed", ex.getMessage());
            }
        });

        profileCard.getChildren().addAll(
                cardTitle,
                new Label("Full Name:"), nameField,
                new Label("Registered Email:"), emailField,
                new Label("Enrollment Number:"), enrollField,
                new Label("Branch / Department:"), branchField,
                new Label("Cumulative GPA (CGPA):"), cgpaField,
                updateBtn
        );

        VBox skillsSection = new VBox(16);
        HBox.setHgrow(skillsSection, Priority.ALWAYS);
        skillsSection.setPadding(new Insets(20));
        skillsSection.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label skillsTitle = new Label("Verified Skills & Proficiencies");
        skillsTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
        skillsTitle.setTextFill(Color.web("#0f172a"));

        skillsTable = new TableView<>();
        skillsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SkillRow, String> skillNameCol = new TableColumn<>("Skill");
        skillNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<SkillRow, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<SkillRow, String> levelCol = new TableColumn<>("Proficiency Level");
        levelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));

        skillsTable.getColumns().addAll(skillNameCol, categoryCol, levelCol);
        VBox.setVgrow(skillsTable, Priority.ALWAYS);

        HBox addSkillBox = new HBox(12);
        addSkillBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Skill> skillDropdown = new ComboBox<>();
        skillDropdown.setPromptText("Select a skill to add");
        skillDropdown.setPrefWidth(220);

        ComboBox<String> levelDropdown = new ComboBox<>();
        levelDropdown.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        levelDropdown.setValue("Intermediate");

        Button addSkillBtn = new Button("Add Skill");
        addSkillBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        Button removeSkillBtn = new Button("Remove Selected");
        removeSkillBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        try {
            List<Skill> catalog = userService.getAllSkills();
            skillDropdown.setItems(FXCollections.observableArrayList(catalog));
        } catch (Exception ignored) {
        }

        addSkillBtn.setOnAction(e -> {
            Skill selectedSkill = skillDropdown.getValue();
            String level = levelDropdown.getValue();
            if (selectedSkill == null) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a skill from the dropdown.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.addSkill(student.getId(), selectedSkill.getId(), level);
                    return null;
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                loadSkillsData();
                loadCompaniesData();
            });

            task.setOnFailed(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Skill Addition Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        removeSkillBtn.setOnAction(e -> {
            SkillRow selected = skillsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a skill row in the table to remove.");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.removeSkill(student.getId(), selected.getSkillId());
                    return null;
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                loadSkillsData();
                loadCompaniesData();
            });

            task.setOnFailed(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Remove Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        addSkillBox.getChildren().addAll(skillDropdown, levelDropdown, addSkillBtn, removeSkillBtn);
        skillsSection.getChildren().addAll(skillsTitle, skillsTable, addSkillBox);

        container.getChildren().addAll(profileCard, skillsSection);
        return container;
    }

    private Parent buildCompaniesTab() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: white;");

        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Placement Drives & Corporate Eligibility");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Drives");
        refreshBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadCompaniesData());

        headerBox.getChildren().addAll(title, spacer, refreshBtn);

        companiesTable = new TableView<>();
        companiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RoleRow, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));

        TableColumn<RoleRow, String> roleCol = new TableColumn<>("Role Title");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoleTitle()));

        TableColumn<RoleRow, String> cgpaCol = new TableColumn<>("Min CGPA");
        cgpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getMinCgpa())));

        TableColumn<RoleRow, String> skillsReqCol = new TableColumn<>("Mandatory Skills");
        skillsReqCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRequiredSkillsStr()));

        TableColumn<RoleRow, String> eligCol = new TableColumn<>("Eligibility Status");
        eligCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isEligible() ? "ELIGIBLE" : "NOT ELIGIBLE"));

        TableColumn<RoleRow, String> appCol = new TableColumn<>("Application Status");
        appCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isApplied() ? "APPLIED" : "NOT APPLIED"));

        companiesTable.getColumns().addAll(companyCol, roleCol, cgpaCol, skillsReqCol, eligCol, appCol);
        VBox.setVgrow(companiesTable, Priority.ALWAYS);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button applyBtn = new Button("Apply to Selected Drive");
        applyBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;");

        applyBtn.setOnAction(e -> {
            RoleRow selected = companiesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainApp.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a job role from the table.");
                return;
            }

            if (selected.isApplied()) {
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Already Applied", "You have already submitted an application for " + selected.getRoleTitle());
                return;
            }

            if (!selected.isEligible()) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Ineligible",
                        "You do not meet the minimum eligibility criteria (CGPA cutoff or required skills) for this drive.");
                return;
            }

            Task<Void> applyTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    placementService.applyToRole(student.getId(), selected.getRoleId());
                    return null;
                }
            };

            applyTask.setOnSucceeded(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Application Submitted",
                        "Successfully applied to " + selected.getRoleTitle() + " at " + selected.getCompanyName() + "!");
                loadCompaniesData();
            });

            applyTask.setOnFailed(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Application Failed", applyTask.getException().getMessage());
            });

            new Thread(applyTask).start();
        });

        actions.getChildren().add(applyBtn);
        container.getChildren().addAll(headerBox, companiesTable, actions);
        return container;
    }

    private void loadSkillsData() {
        Task<List<SkillRow>> task = new Task<>() {
            @Override
            protected List<SkillRow> call() throws Exception {
                Student s = userService.getProfile(student.getId());
                student.setSkillLevels(s.getSkillLevels());
                List<SkillRow> rows = new ArrayList<>();
                for (Map.Entry<Skill, String> entry : s.getSkillLevels().entrySet()) {
                    rows.add(new SkillRow(entry.getKey().getId(), entry.getKey().getName(), entry.getKey().getCategory(), entry.getValue()));
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> {
            if (skillsTable != null) {
                skillsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            }
        });

        new Thread(task).start();
    }

    private void loadCompaniesData() {
        Task<List<RoleRow>> task = new Task<>() {
            @Override
            protected List<RoleRow> call() throws Exception {
                List<Company> companies = placementService.getAllCompanies();
                List<JobRole> eligibleRoles = placementService.getEligibleRoles(student.getId());
                List<Integer> appliedIds = placementService.getAppliedRoleIds(student.getId());

                List<RoleRow> rows = new ArrayList<>();
                for (Company c : companies) {
                    for (JobRole r : c.getJobRoles()) {
                        boolean isEligible = eligibleRoles.stream().anyMatch(er -> er.getRoleId() == r.getRoleId());
                        boolean isApplied = appliedIds.contains(r.getRoleId());

                        StringBuilder sb = new StringBuilder();
                        for (Skill sk : r.getRequiredSkills()) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(sk.getName());
                        }

                        rows.add(new RoleRow(r.getRoleId(), c.getName(), r.getTitle(), r.getMinCgpa(), sb.toString(), isEligible, isApplied));
                    }
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> {
            if (companiesTable != null) {
                companiesTable.setItems(FXCollections.observableArrayList(task.getValue()));
            }
        });

        new Thread(task).start();
    }

    public static class SkillRow {
        private final int skillId;
        private final String name;
        private final String category;
        private final String level;

        public SkillRow(int skillId, String name, String category, String level) {
            this.skillId = skillId;
            this.name = name;
            this.category = category;
            this.level = level;
        }

        public int getSkillId() { return skillId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getLevel() { return level; }
    }

    public static class RoleRow {
        private final int roleId;
        private final String companyName;
        private final String roleTitle;
        private final double minCgpa;
        private final String requiredSkillsStr;
        private final boolean eligible;
        private final boolean applied;

        public RoleRow(int roleId, String companyName, String roleTitle, double minCgpa,
                       String requiredSkillsStr, boolean eligible, boolean applied) {
            this.roleId = roleId;
            this.companyName = companyName;
            this.roleTitle = roleTitle;
            this.minCgpa = minCgpa;
            this.requiredSkillsStr = requiredSkillsStr;
            this.eligible = eligible;
            this.applied = applied;
        }

        public int getRoleId() { return roleId; }
        public String getCompanyName() { return companyName; }
        public String getRoleTitle() { return roleTitle; }
        public double getMinCgpa() { return minCgpa; }
        public String getRequiredSkillsStr() { return requiredSkillsStr; }
        public boolean isEligible() { return eligible; }
        public boolean isApplied() { return applied; }
    }
}

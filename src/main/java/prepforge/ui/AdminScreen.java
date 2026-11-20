package prepforge.ui;

import java.util.ArrayList;
import java.util.List;
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
import prepforge.exception.PrepForgeException;
import prepforge.model.Admin;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.service.PlacementService;
import prepforge.service.UserService;

public class AdminScreen {

    private final Main mainApp;
    private final Admin admin;
    private final UserService userService;
    private final PlacementService placementService;

    private TableView<Student> studentsTable;
    private TableView<CompanyRoleRow> postingsTable;

    public AdminScreen(Main mainApp, Admin admin, UserService userService, PlacementService placementService) {
        this.mainApp = mainApp;
        this.admin = admin;
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
        Tab companiesTab = new Tab("Companies & Job Roles", buildCompaniesAndRolesTab());

        tabPane.getTabs().addAll(studentsTab, companiesTab);
        root.setCenter(tabPane);

        loadStudentsData();
        loadPostingsData();

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
            });

            task.setOnFailed(workerStateEvent -> {
                mainApp.showAlert(Alert.AlertType.ERROR, "Deletion Failed", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        header.getChildren().addAll(title, spacer, refreshBtn, deleteBtn);

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
}

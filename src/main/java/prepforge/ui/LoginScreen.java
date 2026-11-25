package prepforge.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import prepforge.Main;
import prepforge.exception.PrepForgeException;
import prepforge.model.Admin;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.UserService;

public class LoginScreen {

    private final Main mainApp;
    private final UserService userService;

    public LoginScreen(Main mainApp, UserService userService) {
        this.mainApp = mainApp;
        this.userService = userService;
    }

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        VBox heroPane = new VBox(20);
        heroPane.setPrefWidth(380);
        heroPane.setPadding(new Insets(40));
        heroPane.setAlignment(Pos.CENTER_LEFT);
        heroPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3a8a, #3b82f6);");

        Label brandBadge = new Label("ACADEMIC PLACEMENT SUITE");
        brandBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label logoLabel = new Label("PrepForge");
        logoLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
        logoLabel.setTextFill(Color.WHITE);

        Label tagline = new Label("Your Gateway to Campus Placement Success.");
        tagline.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));
        tagline.setTextFill(Color.web("#e0e7ff"));
        tagline.setWrapText(true);

        VBox featuresBox = new VBox(12);
        featuresBox.setPadding(new Insets(30, 0, 0, 0));

        Label f1 = createFeatureBullet("Academic Profile & Skills Management");
        Label f2 = createFeatureBullet("Automated Corporate Eligibility Checking");
        Label f3 = createFeatureBullet("Enterprise Placement Cell Administration");
        featuresBox.getChildren().addAll(f1, f2, f3);

        heroPane.getChildren().addAll(brandBadge, logoLabel, tagline, featuresBox);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white;");

        Tab loginTab = new Tab("Sign In", buildLoginForm());
        Tab registerStudentTab = new Tab("Student Registration", buildStudentRegistrationForm());
        Tab registerAdminTab = new Tab("Admin Registration", buildAdminRegistrationForm());

        tabPane.getTabs().addAll(loginTab, registerStudentTab, registerAdminTab);

        root.setLeft(heroPane);
        root.setCenter(tabPane);

        return root;
    }

    private Label createFeatureBullet(String text) {
        Label bullet = new Label("•  " + text);
        bullet.setTextFill(Color.web("#dbeafe"));
        bullet.setFont(Font.font("SansSerif", 13));
        return bullet;
    }

    private VBox buildLoginForm() {
        VBox form = new VBox(18);
        form.setPadding(new Insets(50, 60, 50, 60));
        form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: white;");

        Label header = new Label("Sign In to PrepForge");
        header.setFont(Font.font("SansSerif", FontWeight.BOLD, 24));
        header.setTextFill(Color.web("#0f172a"));

        Label subHeader = new Label("Enter your credentials to access your portal.");
        subHeader.setTextFill(Color.web("#64748b"));
        subHeader.setFont(Font.font("SansSerif", 13));

        VBox emailBox = new VBox(6);
        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 12));
        emailLabel.setTextFill(Color.web("#334155"));
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. rahul@prepforge.com");
        emailField.setPrefHeight(40);
        emailField.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 0 10;");
        emailBox.getChildren().addAll(emailLabel, emailField);

        VBox passBox = new VBox(6);
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 12));
        passLabel.setTextFill(Color.web("#334155"));
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your secret password");
        passField.setPrefHeight(40);
        passField.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 0 10;");
        passBox.getChildren().addAll(passLabel, passField);

        Button loginBtn = new Button("Sign In");
        loginBtn.setPrefHeight(42);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(26, 26);

        HBox btnBox = new HBox(12, loginBtn, spinner);
        HBox.setHgrow(loginBtn, Priority.ALWAYS);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        loginBtn.setOnAction(e -> {
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String pass = passField.getText();

            loginBtn.setDisable(true);
            spinner.setVisible(true);

            Task<User> loginTask = new Task<>() {
                @Override
                protected User call() throws Exception {
                    return userService.login(email, pass);
                }
            };

            loginTask.setOnSucceeded(workerStateEvent -> {
                loginBtn.setDisable(false);
                spinner.setVisible(false);
                User user = loginTask.getValue();
                if (user instanceof Student) {
                    mainApp.showStudentScreen((Student) user);
                } else if (user instanceof Admin) {
                    mainApp.showAdminScreen((Admin) user);
                } else {
                    mainApp.showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + user.getName());
                }
            });

            loginTask.setOnFailed(workerStateEvent -> {
                loginBtn.setDisable(false);
                spinner.setVisible(false);
                Throwable err = loginTask.getException();
                String msg = (err instanceof PrepForgeException) ? err.getMessage() : "Authentication failed: " + err.getMessage();
                mainApp.showAlert(Alert.AlertType.ERROR, "Login Error", msg);
            });

            Thread th = new Thread(loginTask);
            th.setDaemon(true);
            th.start();
        });

        form.getChildren().addAll(header, subHeader, emailBox, passBox, btnBox);
        return form;
    }

    private VBox buildStudentRegistrationForm() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(30, 60, 30, 60));
        form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: white;");

        Label header = new Label("Register as Student");
        header.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        header.setTextFill(Color.web("#0f172a"));

        Label subHeader = new Label("Create your profile to prepare for campus drives.");
        subHeader.setTextFill(Color.web("#64748b"));

        TextField nameField = createStyledTextField("Full Name (e.g. Rahul Verma)");
        TextField emailField = createStyledTextField("College Email (e.g. rahul@prepforge.com)");
        PasswordField passField = createStyledPasswordField("Password (min 6 characters)");
        PasswordField confirmPassField = createStyledPasswordField("Confirm Password");
        TextField enrollField = createStyledTextField("Enrollment Number (e.g. EN2024_08)");

        ComboBox<String> branchCombo = new ComboBox<>();
        branchCombo.getItems().addAll("Computer Science & Engineering", "Information Technology",
                "Electronics & Communication", "Mechanical Engineering", "Electrical Engineering");
        branchCombo.setValue("Computer Science & Engineering");
        branchCombo.setPrefHeight(38);
        branchCombo.setMaxWidth(Double.MAX_VALUE);

        TextField cgpaField = createStyledTextField("Cumulative GPA (0.00 - 10.00, e.g. 8.50)");

        Button registerBtn = new Button("Register Student Account");
        registerBtn.setPrefHeight(42);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(24, 24);

        HBox actionBox = new HBox(12, registerBtn, spinner);
        HBox.setHgrow(registerBtn, Priority.ALWAYS);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText() != null ? nameField.getText().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String pass = passField.getText();
            String confirmPass = confirmPassField.getText();
            if (pass == null || pass.isEmpty() || !pass.equals(confirmPass)) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Password Mismatch", "Passwords do not match. Please verify your password.");
                return;
            }
            String enroll = enrollField.getText() != null ? enrollField.getText().trim() : "";
            String branch = branchCombo.getValue();
            String cgpaStr = cgpaField.getText() != null ? cgpaField.getText().trim() : "";

            double cgpa;
            try {
                cgpa = Double.parseDouble(cgpaStr);
            } catch (NumberFormatException ex) {
                mainApp.showAlert(Alert.AlertType.ERROR, "Input Error", "CGPA must be a valid numeric decimal (e.g. 8.5).");
                return;
            }

            registerBtn.setDisable(true);
            spinner.setVisible(true);

            Task<Void> regTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Student student = new Student(name, email, enroll, branch, cgpa);
                    userService.registerStudent(student, pass);
                    return null;
                }
            };

            regTask.setOnSucceeded(event -> {
                registerBtn.setDisable(false);
                spinner.setVisible(false);
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                        "Account created successfully for " + name + "! Please sign in with your credentials.");
                nameField.clear();
                emailField.clear();
                passField.clear();
                confirmPassField.clear();
                enrollField.clear();
                cgpaField.clear();
            });

            regTask.setOnFailed(event -> {
                registerBtn.setDisable(false);
                spinner.setVisible(false);
                Throwable err = regTask.getException();
                String msg = (err instanceof PrepForgeException) ? err.getMessage() : "Registration failed: " + err.getMessage();
                mainApp.showAlert(Alert.AlertType.ERROR, "Registration Error", msg);
            });

            Thread th = new Thread(regTask);
            th.setDaemon(true);
            th.start();
        });

        form.getChildren().addAll(header, subHeader,
                new Label("Full Name"), nameField,
                new Label("Email Address"), emailField,
                new Label("Password"), passField,
                new Label("Confirm Password"), confirmPassField,
                new Label("Enrollment Number"), enrollField,
                new Label("Department / Branch"), branchCombo,
                new Label("Current CGPA"), cgpaField,
                actionBox);
        return form;
    }

    private VBox buildAdminRegistrationForm() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(30, 60, 30, 60));
        form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: white;");

        Label header = new Label("Register Placement Officer");
        header.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        header.setTextFill(Color.web("#0f172a"));

        Label subHeader = new Label("Create an administrative account for company and placement management.");
        subHeader.setTextFill(Color.web("#64748b"));

        TextField nameField = createStyledTextField("Full Name (e.g. Dr. A. P. Sharma)");
        TextField emailField = createStyledTextField("Official Email (e.g. admin@prepforge.com)");
        PasswordField passField = createStyledPasswordField("Password (min 6 characters)");
        PasswordField confirmPassField = createStyledPasswordField("Confirm Password");
        TextField empIdField = createStyledTextField("Employee ID (e.g. TPO_101)");
        TextField deptField = createStyledTextField("Department (e.g. Training & Placement Cell)");

        Button registerBtn = new Button("Register Administrator");
        registerBtn.setPrefHeight(42);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(24, 24);

        HBox actionBox = new HBox(12, registerBtn, spinner);
        HBox.setHgrow(registerBtn, Priority.ALWAYS);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText() != null ? nameField.getText().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String pass = passField.getText();
            String confirmPass = confirmPassField.getText();
            if (pass == null || pass.isEmpty() || !pass.equals(confirmPass)) {
                mainApp.showAlert(Alert.AlertType.WARNING, "Password Mismatch", "Passwords do not match. Please verify your password.");
                return;
            }
            String empId = empIdField.getText() != null ? empIdField.getText().trim() : "";
            String dept = deptField.getText() != null ? deptField.getText().trim() : "";

            registerBtn.setDisable(true);
            spinner.setVisible(true);

            Task<Void> regTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Admin admin = new Admin(name, email, empId, dept);
                    userService.registerAdmin(admin, pass);
                    return null;
                }
            };

            regTask.setOnSucceeded(event -> {
                registerBtn.setDisable(false);
                spinner.setVisible(false);
                mainApp.showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                        "Administrator account created successfully! Please sign in with your credentials.");
                nameField.clear();
                emailField.clear();
                passField.clear();
                confirmPassField.clear();
                empIdField.clear();
                deptField.clear();
            });

            regTask.setOnFailed(event -> {
                registerBtn.setDisable(false);
                spinner.setVisible(false);
                Throwable err = regTask.getException();
                String msg = (err instanceof PrepForgeException) ? err.getMessage() : "Registration failed: " + err.getMessage();
                mainApp.showAlert(Alert.AlertType.ERROR, "Registration Error", msg);
            });

            Thread th = new Thread(regTask);
            th.setDaemon(true);
            th.start();
        });

        form.getChildren().addAll(header, subHeader,
                new Label("Full Name"), nameField,
                new Label("Official Email"), emailField,
                new Label("Password"), passField,
                new Label("Confirm Password"), confirmPassField,
                new Label("Employee ID"), empIdField,
                new Label("Department"), deptField,
                actionBox);
        return form;
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(38);
        tf.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 0 10;");
        return tf;
    }

    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(38);
        pf.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 0 10;");
        return pf;
    }
}

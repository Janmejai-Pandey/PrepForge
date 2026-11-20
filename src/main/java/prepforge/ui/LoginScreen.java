package prepforge.ui;

import javafx.application.Platform;
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
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.UserService;

/**
 * UI Screen: LoginScreen
 * 
 * Contract Owner: Member 4 (UI & Integration)
 * 
 * Handles user authentication, student registration, and admin registration.
 * Routes authenticated users polymorphically to StudentScreen or AdminScreen.
 * Executes database operations asynchronously within JavaFX Task workers.
 */
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

        // ---------------- Left Hero Pane ----------------
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

        Label tagline = new Label("Your Intelligent Gateway to Campus Placement Success.");
        tagline.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));
        tagline.setTextFill(Color.web("#e0e7ff"));
        tagline.setWrapText(true);

        VBox featureList = new VBox(14);
        featureList.setPadding(new Insets(20, 0, 0, 0));
        featureList.getChildren().addAll(
                createFeatureRow("✓", "Real-Time Company Placement Eligibility Matching"),
                createFeatureRow("✓", "Adaptive Mock Interviews with Automated Grading"),
                createFeatureRow("✓", "Weak-Skill Diagnostics (<50% Threshold) & Growth Plans"),
                createFeatureRow("✓", "Cryptographic Security (PBKDF2 SHA-256 Hashing)")
        );

        heroPane.getChildren().addAll(brandBadge, logoLabel, tagline, featureList);

        // ---------------- Right Content Pane (TabPane) ----------------
        VBox centerCard = new VBox(15);
        centerCard.setAlignment(Pos.CENTER);
        centerCard.setPadding(new Insets(40, 60, 40, 60));

        TabPane authTabs = new TabPane();
        authTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        authTabs.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        Tab loginTab = new Tab("Sign In", createLoginForm());
        Tab studentRegTab = new Tab("Register Student", createStudentRegisterForm());
        Tab adminRegTab = new Tab("Register Admin", createAdminRegisterForm());

        authTabs.getTabs().addAll(loginTab, studentRegTab, adminRegTab);
        centerCard.getChildren().add(authTabs);

        root.setLeft(heroPane);
        root.setCenter(centerCard);

        return root;
    }

    private HBox createFeatureRow(String icon, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: #93c5fd; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 13px;");
        textLabel.setWrapText(true);
        row.getChildren().addAll(iconLabel, textLabel);
        return row;
    }

    // ---------------- 1. Sign In Form ----------------
    private VBox createLoginForm() {
        VBox form = new VBox(16);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Welcome Back");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #1e293b;");

        Label subtitle = new Label("Enter your credentials to access your PrepForge portal");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        TextField emailField = new TextField();
        emailField.setPromptText("student@college.edu or admin@prepforge.com");
        styleInput(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        styleInput(passwordField);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(22, 22);

        Button loginBtn = new Button("Sign In to Portal");
        stylePrimaryButton(loginBtn);

        HBox btnBox = new HBox(12, loginBtn, spinner);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Label hintLabel = new Label("Quick Test: Register a new student or use an existing test account.");
        hintLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");

        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            spinner.setVisible(true);
            loginBtn.setDisable(true);

            // Execute database/hashing operations in background Task (No UI freezing)
            Task<User> loginTask = new Task<>() {
                @Override
                protected User call() throws Exception {
                    return userService.login(email, password);
                }
            };

            loginTask.setOnSucceeded(workerEvent -> {
                spinner.setVisible(false);
                loginBtn.setDisable(false);
                User user = loginTask.getValue();

                // Polymorphic routing based on User Role
                if ("ADMIN".equalsIgnoreCase(user.getRole()) && user instanceof Admin) {
                    mainApp.showAdminScreen((Admin) user);
                } else if (user instanceof Student) {
                    mainApp.showStudentScreen((Student) user);
                } else {
                    Main.showErrorAlert("Login Error", "Unknown account role: " + user.getRole());
                }
            });

            loginTask.setOnFailed(workerEvent -> {
                spinner.setVisible(false);
                loginBtn.setDisable(false);
                Throwable ex = loginTask.getException();
                Main.showErrorAlert("Sign In Failed", ex.getMessage());
            });

            new Thread(loginTask).start();
        });

        form.getChildren().addAll(title, subtitle, new Label("Email Address:"), emailField,
                new Label("Password:"), passwordField, btnBox, hintLabel);
        return form;
    }

    // ---------------- 2. Student Registration Form ----------------
    private VBox createStudentRegisterForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Create Student Account");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #1e293b;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        styleInput(nameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email (e.g. rahul@college.edu)");
        styleInput(emailField);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password (at least 6 characters)");
        styleInput(passField);

        TextField enrollField = new TextField();
        enrollField.setPromptText("Enrollment Number (e.g. EN2024_001)");
        styleInput(enrollField);

        ComboBox<String> branchCombo = new ComboBox<>();
        branchCombo.getItems().addAll("Computer Science & Engineering", "Information Technology",
                "Electronics & Communication", "Mechanical Engineering", "Civil Engineering");
        branchCombo.setValue("Computer Science & Engineering");
        branchCombo.setMaxWidth(Double.MAX_VALUE);

        TextField cgpaField = new TextField();
        cgpaField.setPromptText("Current CGPA (0.0 to 10.0, e.g. 8.4)");
        styleInput(cgpaField);

        Button registerBtn = new Button("Register as Student");
        stylePrimaryButton(registerBtn);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(22, 22);

        registerBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String pass = passField.getText();
                String enroll = enrollField.getText();
                String branch = branchCombo.getValue();
                double cgpa = Double.parseDouble(cgpaField.getText().trim());

                Student student = new Student(name, email, enroll, branch, cgpa);

                spinner.setVisible(true);
                registerBtn.setDisable(true);

                Task<Void> regTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        userService.registerStudent(student, pass);
                        return null;
                    }
                };

                regTask.setOnSucceeded(ev -> {
                    spinner.setVisible(false);
                    registerBtn.setDisable(false);
                    Main.showSuccessAlert("Registration Successful", "Your student account has been created!\nPlease sign in.");
                    nameField.clear();
                    emailField.clear();
                    passField.clear();
                    enrollField.clear();
                    cgpaField.clear();
                });

                regTask.setOnFailed(ev -> {
                    spinner.setVisible(false);
                    registerBtn.setDisable(false);
                    Main.showErrorAlert("Registration Failed", regTask.getException().getMessage());
                });

                new Thread(regTask).start();

            } catch (NumberFormatException nfe) {
                Main.showErrorAlert("Invalid Input", "Please enter a valid numeric CGPA (e.g. 8.5).");
            } catch (Exception ex) {
                Main.showErrorAlert("Input Error", ex.getMessage());
            }
        });

        HBox btnBox = new HBox(12, registerBtn, spinner);
        form.getChildren().addAll(title,
                new Label("Full Name:"), nameField,
                new Label("College Email:"), emailField,
                new Label("Password:"), passField,
                new Label("Enrollment Number:"), enrollField,
                new Label("Academic Branch:"), branchCombo,
                new Label("Current CGPA:"), cgpaField,
                btnBox);
        return form;
    }

    // ---------------- 3. Admin Registration Form ----------------
    private VBox createAdminRegisterForm() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(24));
        form.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Register Placement Administrator");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #1e293b;");

        TextField nameField = new TextField();
        nameField.setPromptText("Officer Name (e.g. Dr. Verma)");
        styleInput(nameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Official Email");
        styleInput(emailField);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Master Password");
        styleInput(passField);

        TextField deptField = new TextField("Training and Placement Cell (TPO)");
        styleInput(deptField);

        TextField codeField = new TextField();
        codeField.setPromptText("Employee / Faculty Code (e.g. TPO_101)");
        styleInput(codeField);

        Button registerBtn = new Button("Register as Admin");
        stylePrimaryButton(registerBtn);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(22, 22);

        registerBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String pass = passField.getText();
                String dept = deptField.getText();
                String code = codeField.getText();

                Admin admin = new Admin(name, email, dept, code);

                spinner.setVisible(true);
                registerBtn.setDisable(true);

                Task<Void> regTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        userService.registerAdmin(admin, pass);
                        return null;
                    }
                };

                regTask.setOnSucceeded(ev -> {
                    spinner.setVisible(false);
                    registerBtn.setDisable(false);
                    Main.showSuccessAlert("Admin Created", "Administrator profile created successfully! You may now sign in.");
                    nameField.clear();
                    emailField.clear();
                    passField.clear();
                    codeField.clear();
                });

                regTask.setOnFailed(ev -> {
                    spinner.setVisible(false);
                    registerBtn.setDisable(false);
                    Main.showErrorAlert("Registration Failed", regTask.getException().getMessage());
                });

                new Thread(regTask).start();

            } catch (Exception ex) {
                Main.showErrorAlert("Input Error", ex.getMessage());
            }
        });

        HBox btnBox = new HBox(12, registerBtn, spinner);
        form.getChildren().addAll(title,
                new Label("Official Name:"), nameField,
                new Label("Official Email:"), emailField,
                new Label("Password:"), passField,
                new Label("Department:"), deptField,
                new Label("Employee Code:"), codeField,
                btnBox);
        return form;
    }

    // ---------------- Styling Helpers ----------------

    private void styleInput(TextField tf) {
        tf.setPrefHeight(38);
        tf.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-font-size: 13px;");
    }

    private void stylePrimaryButton(Button btn) {
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 13px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 13px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 13px;"));
    }
}

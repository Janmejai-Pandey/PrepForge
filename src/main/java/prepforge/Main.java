package prepforge;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import prepforge.model.Admin;
import prepforge.model.Interview;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.AnalyticsService;
import prepforge.service.InterviewService;
import prepforge.service.PlacementService;
import prepforge.service.UserService;
import prepforge.ui.AdminScreen;
import prepforge.ui.InterviewScreen;
import prepforge.ui.LoginScreen;
import prepforge.ui.StudentScreen;

/**
 * Main Application Entry Point
 * 
 * Contract Owner: Member 4 (UI & Integration)
 * 
 * Manages global application state, active logged-in user session, service dependencies,
 * and high-level scene transitions between Login, Student Portal, Admin Portal, and Mock Interview.
 */
public class Main extends Application {

    private Stage primaryStage;
    private User currentUser;

    // Core Service Dependencies (Integration of M1 and M3)
    private UserService userService;
    private PlacementService placementService;
    private InterviewService interviewService;
    private AnalyticsService analyticsService;

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize services (Thread-safe singleton service instances)
        this.userService = new UserService();
        this.placementService = new PlacementService();
        this.interviewService = new InterviewService();
        this.analyticsService = new AnalyticsService(this.interviewService, this.userService, this.placementService);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("PrepForge — Placement Preparation & Mock Interview Platform");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        showLoginScreen();
        primaryStage.show();
    }

    // ---------------- Screen Navigation ----------------

    public void showLoginScreen() {
        this.currentUser = null;
        LoginScreen loginScreen = new LoginScreen(this, userService);
        Scene scene = new Scene(loginScreen.createView(), 1050, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("PrepForge — Sign In & Registration");
    }

    public void showStudentScreen(Student student) {
        this.currentUser = student;
        StudentScreen studentScreen = new StudentScreen(this, student, userService, placementService, interviewService, analyticsService);
        Scene scene = new Scene(studentScreen.createView(), 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setTitle("PrepForge — Student Dashboard [" + student.getName() + "]");
    }

    public void showAdminScreen(Admin admin) {
        this.currentUser = admin;
        AdminScreen adminScreen = new AdminScreen(this, admin, userService, placementService, interviewService, analyticsService);
        Scene scene = new Scene(adminScreen.createView(), 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setTitle("PrepForge — Placement Administration Portal [" + admin.getName() + "]");
    }

    public void showInterviewScreen(Student student, Interview interview) {
        InterviewScreen interviewScreen = new InterviewScreen(this, student, interview, interviewService);
        Scene scene = new Scene(interviewScreen.createView(), 1050, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("PrepForge — Live Interview Session [" + interview.getInterviewType() + "]");
    }

    public void logout() {
        this.currentUser = null;
        showLoginScreen();
    }

    // ---------------- Utility Alert Methods ----------------

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showErrorAlert(String title, String errorMessage) {
        showAlert(Alert.AlertType.ERROR, title, "An error occurred", errorMessage);
    }

    public static void showSuccessAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, "Success", message);
    }

    // ---------------- Getters ----------------

    public User getCurrentUser() {
        return currentUser;
    }

    public UserService getUserService() {
        return userService;
    }

    public PlacementService getPlacementService() {
        return placementService;
    }

    public InterviewService getInterviewService() {
        return interviewService;
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

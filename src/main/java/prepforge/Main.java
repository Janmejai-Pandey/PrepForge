package prepforge;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import prepforge.model.Admin;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.PlacementService;
import prepforge.service.UserService;
import prepforge.ui.AdminScreen;
import prepforge.ui.LoginScreen;
import prepforge.ui.StudentScreen;

public class Main extends Application {

    private Stage primaryStage;
    private User currentUser;
    private UserService userService;
    private PlacementService placementService;

    @Override
    public void init() throws Exception {
        super.init();
        this.userService = new UserService();
        this.placementService = new PlacementService();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("PrepForge — Placement Preparation Platform");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        showLoginScreen();
        primaryStage.show();
    }

    public void showLoginScreen() {
        this.currentUser = null;
        LoginScreen loginScreen = new LoginScreen(this, userService);
        Scene scene = new Scene(loginScreen.createView(), 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void showStudentScreen(Student student) {
        this.currentUser = student;
        StudentScreen studentScreen = new StudentScreen(this, student, userService, placementService);
        Scene scene = new Scene(studentScreen.createView(), 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void showAdminScreen(Admin admin) {
        this.currentUser = admin;
        AdminScreen adminScreen = new AdminScreen(this, admin, userService, placementService);
        Scene scene = new Scene(adminScreen.createView(), 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void logout() {
        this.currentUser = null;
        showLoginScreen();
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type, message, ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.initOwner(primaryStage);
            alert.showAndWait();
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public UserService getUserService() {
        return userService;
    }

    public PlacementService getPlacementService() {
        return placementService;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

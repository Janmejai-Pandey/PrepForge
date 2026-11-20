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
import prepforge.model.Interview;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.model.Student;
import prepforge.service.InterviewService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UI Screen: InterviewScreen
 * 
 * Contract Owner: Member 4 (UI & Integration)
 * 
 * Implements a question-by-question live interview examination.
 * Dynamically renders multiple-choice options or open text areas based on question polymorphism.
 * Submits the interview in an asynchronous Task and displays the evaluated score and grade.
 */
public class InterviewScreen {

    private final Main mainApp;
    private final Student student;
    private final Interview interview;
    private final InterviewService interviewService;

    private int currentIndex = 0;
    private final List<Question> questions;
    private final Map<Integer, String> studentAnswers = new HashMap<>();

    // Dynamic UI Elements
    private Label progressLabel;
    private ProgressBar progressBar;
    private Label topicBadge;
    private Label diffBadge;
    private Label marksBadge;
    private Label questionTextLabel;
    private VBox answerContainer;
    private Button prevBtn;
    private Button nextBtn;
    private Button submitBtn;

    public InterviewScreen(Main mainApp, Student student, Interview interview, InterviewService interviewService) {
        this.mainApp = mainApp;
        this.student = student;
        this.interview = interview;
        this.interviewService = interviewService;
        this.questions = interview.getQuestions();
    }

    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        // ---------------- Top Header Bar ----------------
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(16, 24, 16, 24));
        topBox.setStyle("-fx-background-color: #0f172a;");

        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("PrepForge Live Assessment — " + interview.getInterviewType() + " Interview");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exitBtn = new Button("Exit Assessment");
        exitBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-background-radius: 6; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit? Unsubmitted answers will be lost.", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) mainApp.showStudentScreen(student);
            });
        });

        topRow.getChildren().addAll(title, spacer, exitBtn);

        // Progress row
        HBox progressRow = new HBox(12);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        progressLabel = new Label("Question 1 of " + questions.size());
        progressLabel.setTextFill(Color.web("#94a3b8"));
        progressLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 12));

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #3b82f6;");

        progressRow.getChildren().addAll(progressLabel, progressBar);
        topBox.getChildren().addAll(topRow, progressRow);
        root.setTop(topBox);

        // ---------------- Center Question Card ----------------
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(30, 40, 30, 40));
        centerBox.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(18);
        card.setPadding(new Insets(30));
        card.setMaxWidth(850);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        // Question Badges
        HBox badgeBox = new HBox(10);
        badgeBox.setAlignment(Pos.CENTER_LEFT);

        topicBadge = createBadge("Topic: Java", "#e0e7ff", "#3730a3");
        diffBadge = createBadge("MEDIUM", "#fef3c7", "#92400e");
        marksBadge = createBadge("10.0 Marks", "#dcfce7", "#166534");

        badgeBox.getChildren().addAll(topicBadge, diffBadge, marksBadge);

        questionTextLabel = new Label();
        questionTextLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 17));
        questionTextLabel.setStyle("-fx-text-fill: #1e293b;");
        questionTextLabel.setWrapText(true);

        answerContainer = new VBox(12);
        answerContainer.setPadding(new Insets(10, 0, 10, 0));

        card.getChildren().addAll(badgeBox, questionTextLabel, new Separator(), answerContainer);
        centerBox.getChildren().add(card);
        root.setCenter(centerBox);

        // ---------------- Bottom Navigation Bar ----------------
        HBox bottomBar = new HBox(16);
        bottomBar.setPadding(new Insets(16, 40, 24, 40));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        prevBtn = new Button("← Previous Question");
        prevBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 20; -fx-cursor: hand;");
        prevBtn.setOnAction(e -> navigateQuestion(-1));

        nextBtn = new Button("Save & Next Question →");
        nextBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 20; -fx-cursor: hand;");
        nextBtn.setOnAction(e -> navigateQuestion(1));

        submitBtn = new Button("Finish & Submit Assessment ✓");
        submitBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 24; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> submitAssessment());

        Region bSpacer = new Region();
        HBox.setHgrow(bSpacer, Priority.ALWAYS);

        bottomBar.getChildren().addAll(prevBtn, bSpacer, nextBtn, submitBtn);
        root.setBottom(bottomBar);

        // Load initial question
        if (!questions.isEmpty()) {
            displayQuestion(0);
        }

        return root;
    }

    private Label createBadge(String text, String bgColor, String textColor) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor
                + "; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
        return badge;
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;

        Question q = questions.get(index);

        progressLabel.setText("Question " + (index + 1) + " of " + questions.size());
        progressBar.setProgress((double) (index + 1) / questions.size());

        topicBadge.setText("Topic: " + q.getTopic());
        diffBadge.setText("Difficulty: " + q.getDifficulty());
        marksBadge.setText(q.getMaxMarks() + " Marks");

        questionTextLabel.setText(q.getText());

        answerContainer.getChildren().clear();

        // Render appropriate response input by Question Polymorphism
        if (q instanceof MCQQuestion) {
            MCQQuestion mcq = (MCQQuestion) q;
            ToggleGroup optionsGroup = new ToggleGroup();
            String savedAnswer = studentAnswers.get(q.getId());

            for (String opt : mcq.getOptions()) {
                RadioButton rb = new RadioButton(opt);
                rb.setToggleGroup(optionsGroup);
                rb.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-padding: 6;");

                if (savedAnswer != null && savedAnswer.equalsIgnoreCase(opt)) {
                    rb.setSelected(true);
                }

                rb.setOnAction(e -> {
                    studentAnswers.put(q.getId(), opt);
                    interview.recordAnswer(q.getId(), opt);
                });
                answerContainer.getChildren().add(rb);
            }
        } else {
            // Text area for Technical, Coding, and HR questions
            TextArea ta = new TextArea();
            ta.setPromptText("Enter your answer, technical explanation, or code approach here...");
            ta.setPrefRowCount(7);
            ta.setWrapText(true);
            ta.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-font-size: 13px; -fx-font-family: 'Consolas', monospace;");

            String saved = studentAnswers.get(q.getId());
            if (saved != null) {
                ta.setText(saved);
            }

            ta.textProperty().addListener((obs, oldVal, newVal) -> {
                studentAnswers.put(q.getId(), newVal);
                interview.recordAnswer(q.getId(), newVal);
            });

            answerContainer.getChildren().add(ta);
        }

        prevBtn.setDisable(currentIndex == 0);
        nextBtn.setVisible(currentIndex < questions.size() - 1);
    }

    private void navigateQuestion(int delta) {
        int next = currentIndex + delta;
        if (next >= 0 && next < questions.size()) {
            displayQuestion(next);
        }
    }

    private void submitAssessment() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Submit all your answers for automated grading and result generation?",
                ButtonType.YES, ButtonType.CANCEL);
        confirm.setTitle("Confirm Submission");
        confirm.setHeaderText("Ready to Submit?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // Background evaluation Task
                Task<Result> task = new Task<>() {
                    @Override
                    protected Result call() throws Exception {
                        return interviewService.submitInterview(interview);
                    }
                };

                task.setOnSucceeded(ev -> {
                    Result res = task.getValue();
                    showScorecardModal(res);
                });

                task.setOnFailed(ev -> Main.showErrorAlert("Evaluation Failed", task.getException().getMessage()));
                new Thread(task).start();
            }
        });
    }

    private void showScorecardModal(Result res) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Interview Assessment Scorecard");
        dialog.setHeaderText("Mock Interview Evaluated Successfully!");

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label gradeLabel = new Label("Grade Awarded: " + res.getGrade());
        gradeLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 26));

        if (res.getPercentage() >= 75.0) {
            gradeLabel.setStyle("-fx-text-fill: #16a34a;");
        } else if (res.getPercentage() >= 50.0) {
            gradeLabel.setStyle("-fx-text-fill: #d97706;");
        } else {
            gradeLabel.setStyle("-fx-text-fill: #dc2626;");
        }

        Label scoreDetail = new Label("Marks: " + res.getTotalScore() + " / " + res.getMaxScore()
                + " (" + res.getPercentage() + "%)");
        scoreDetail.setFont(Font.font("SansSerif", FontWeight.NORMAL, 16));

        Label note = new Label("Your score has been registered. View personalized skill recommendations and placement eligibility on your dashboard.");
        note.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        note.setWrapText(true);

        content.getChildren().addAll(gradeLabel, scoreDetail, note);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
        mainApp.showStudentScreen(student);
    }
}

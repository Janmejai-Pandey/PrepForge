package prepforge.ui;

import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import prepforge.Main;
import prepforge.model.CodingQuestion;
import prepforge.model.Interview;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.model.Student;
import prepforge.service.InterviewService;

public class InterviewScreen {

    private final Main mainApp;
    private final Student student;
    private final Interview interview;
    private final InterviewService interviewService;

    private int currentIndex = 0;
    private final List<Question> questions;

    private BorderPane root;
    private VBox questionBox;
    private Label progressLabel;
    private ProgressBar progressBar;
    private ToggleGroup mcqGroup;
    private TextArea textAnswerArea;

    public InterviewScreen(Main mainApp, Student student, Interview interview, InterviewService interviewService) {
        this.mainApp = mainApp;
        this.student = student;
        this.interview = interview;
        this.interviewService = interviewService;
        this.questions = interview.getQuestions();
    }

    public Parent createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(16, 28, 16, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label brand = new Label("PrepForge Assessment");
        brand.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        brand.setTextFill(Color.web("#1e40af"));

        Label typeBadge = new Label(interview.getType() + " (" + interview.getDifficulty() + ")");
        typeBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        progressLabel = new Label("Question 1 of " + questions.size());
        progressLabel.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 14));
        progressLabel.setTextFill(Color.web("#475569"));

        progressBar = new ProgressBar(1.0 / questions.size());
        progressBar.setPrefWidth(200);

        topBar.getChildren().addAll(brand, typeBadge, spacer, progressLabel, progressBar);
        root.setTop(topBar);

        questionBox = new VBox(20);
        questionBox.setPadding(new Insets(32, 60, 32, 60));
        questionBox.setAlignment(Pos.TOP_LEFT);

        root.setCenter(questionBox);
        renderCurrentQuestion();

        return root;
    }

    private void renderCurrentQuestion() {
        questionBox.getChildren().clear();

        if (questions.isEmpty()) {
            Label noQ = new Label("No questions available for this interview.");
            questionBox.getChildren().add(noQ);
            return;
        }

        Question q = questions.get(currentIndex);

        progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size());
        progressBar.setProgress((double) (currentIndex + 1) / questions.size());

        HBox metaBox = new HBox(12);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label qType = new Label(q.getType());
        qType.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label topicLabel = new Label(q.getTopic());
        topicLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label marksLabel = new Label(q.getMaxMarks() + " Marks");
        marksLabel.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

        metaBox.getChildren().addAll(qType, topicLabel, marksLabel);

        Label textLabel = new Label(q.getText());
        textLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        textLabel.setTextFill(Color.web("#0f172a"));
        textLabel.setWrapText(true);

        questionBox.getChildren().addAll(metaBox, textLabel);

        String savedAnswer = interview.getAnswer(q.getId());

        if (q instanceof MCQQuestion) {
            MCQQuestion mcq = (MCQQuestion) q;
            mcqGroup = new ToggleGroup();
            VBox optionsBox = new VBox(12);
            optionsBox.setPadding(new Insets(10, 0, 10, 0));

            List<String> options = mcq.getOptions();
            for (int i = 0; i < options.size(); i++) {
                String letter = String.valueOf((char) ('A' + i));
                RadioButton rb = new RadioButton(letter + ") " + options.get(i));
                rb.setToggleGroup(mcqGroup);
                rb.setUserData(letter);
                rb.setFont(Font.font("SansSerif", 14));

                if (savedAnswer != null && (savedAnswer.equalsIgnoreCase(letter) || savedAnswer.equalsIgnoreCase(options.get(i)))) {
                    rb.setSelected(true);
                }
                optionsBox.getChildren().add(rb);
            }
            textAnswerArea = null;
            questionBox.getChildren().add(optionsBox);
        } else {
            mcqGroup = null;
            if (q instanceof CodingQuestion) {
                Label prompt = new Label(((CodingQuestion) q).getProblemStatement());
                prompt.setFont(Font.font("Monospaced", 13));
                prompt.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
                prompt.setWrapText(true);
                questionBox.getChildren().add(prompt);
            }

            textAnswerArea = new TextArea(savedAnswer != null ? savedAnswer : "");
            textAnswerArea.setPromptText("Enter your detailed technical answer or code implementation here...");
            textAnswerArea.setPrefRowCount(8);
            textAnswerArea.setWrapText(true);
            textAnswerArea.setFont(Font.font("SansSerif", 13));
            textAnswerArea.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 6;");

            questionBox.getChildren().add(textAnswerArea);
        }

        HBox navBox = new HBox(16);
        navBox.setPadding(new Insets(20, 0, 0, 0));
        navBox.setAlignment(Pos.CENTER_LEFT);

        Button prevBtn = new Button("Previous");
        prevBtn.setDisable(currentIndex == 0);
        prevBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;");
        prevBtn.setOnAction(e -> {
            saveCurrentAnswer();
            currentIndex--;
            renderCurrentQuestion();
        });

        Button nextBtn = new Button("Next");
        nextBtn.setDisable(currentIndex == questions.size() - 1);
        nextBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;");
        nextBtn.setOnAction(e -> {
            saveCurrentAnswer();
            currentIndex++;
            renderCurrentQuestion();
        });

        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);

        Button submitBtn = new Button("Submit Assessment");
        submitBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(24, 24);

        submitBtn.setOnAction(e -> {
            saveCurrentAnswer();
            submitBtn.setDisable(true);
            spinner.setVisible(true);

            Task<Result> task = new Task<>() {
                @Override
                protected Result call() throws Exception {
                    return interviewService.submitInterview(interview);
                }
            };

            task.setOnSucceeded(workerStateEvent -> {
                spinner.setVisible(false);
                renderResultScreen(task.getValue());
            });

            task.setOnFailed(workerStateEvent -> {
                submitBtn.setDisable(false);
                spinner.setVisible(false);
                mainApp.showAlert(Alert.AlertType.ERROR, "Submission Error", task.getException().getMessage());
            });

            new Thread(task).start();
        });

        navBox.getChildren().addAll(prevBtn, nextBtn, navSpacer, submitBtn, spinner);
        questionBox.getChildren().add(navBox);
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty() || currentIndex < 0 || currentIndex >= questions.size()) {
            return;
        }
        Question q = questions.get(currentIndex);
        String ans = "";

        if (q instanceof MCQQuestion && mcqGroup != null) {
            if (mcqGroup.getSelectedToggle() != null) {
                ans = mcqGroup.getSelectedToggle().getUserData().toString();
            }
        } else if (textAnswerArea != null) {
            ans = textAnswerArea.getText().trim();
        }

        try {
            interviewService.submitAnswer(interview, q.getId(), ans);
        } catch (Exception ignored) {
        }
    }

    private void renderResultScreen(Result result) {
        questionBox.getChildren().clear();
        root.setTop(null);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12;");
        card.setMaxWidth(600);

        Label title = new Label("Assessment Completed!");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#0f172a"));

        Label scoreLabel = new Label(String.format("%.1f / %.1f Marks", result.getTotalScore(), result.getMaxScore()));
        scoreLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 32));
        scoreLabel.setTextFill(Color.web("#2563eb"));

        Label pctLabel = new Label(String.format("Final Percentage: %.1f%%", result.getPercentage()));
        pctLabel.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 18));
        pctLabel.setTextFill(Color.web("#475569"));

        Label gradeBadge = new Label("Evaluation Grade: " + result.getGrade());
        gradeBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 6 16; -fx-background-radius: 16; -fx-font-size: 16px; -fx-font-weight: bold;");

        Button doneBtn = new Button("Return to Student Dashboard");
        doneBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 24;");
        doneBtn.setOnAction(e -> mainApp.showStudentScreen(student));

        card.getChildren().addAll(title, scoreLabel, pctLabel, gradeBadge, doneBtn);

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40));
        root.setCenter(wrapper);
    }
}

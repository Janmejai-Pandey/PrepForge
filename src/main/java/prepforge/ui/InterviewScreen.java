package prepforge.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import prepforge.Main;
import prepforge.model.CodingQuestion;
import prepforge.model.Interview;
import prepforge.model.KeywordBased;
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
    private Label timerLabel;
    private ToggleGroup mcqGroup;
    private TextArea textAnswerArea;

    private int remainingSeconds;
    private Timeline countdownTimer;
    private FlowPane questionPalette;
    private final List<Button> paletteButtons = new ArrayList<>();

    public InterviewScreen(Main mainApp, Student student, Interview interview, InterviewService interviewService) {
        this.mainApp = mainApp;
        this.student = student;
        this.interview = interview;
        this.interviewService = interviewService;
        this.questions = interview.getQuestions();
        this.remainingSeconds = Math.max(1, questions.size()) * 120;
    }

    public Parent createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox topBar = new HBox(16);
        topBar.setPadding(new Insets(14, 28, 14, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label brand = new Label("PrepForge Assessment");
        brand.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        brand.setTextFill(Color.web("#1e40af"));

        Label typeBadge = new Label(interview.getType() + " (" + interview.getDifficulty() + ")");
        typeBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        timerLabel = new Label();
        timerLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));
        updateTimerDisplay();

        progressLabel = new Label("Question 1 of " + questions.size());
        progressLabel.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 13));
        progressLabel.setTextFill(Color.web("#475569"));

        progressBar = new ProgressBar(questions.isEmpty() ? 0 : 1.0 / questions.size());
        progressBar.setPrefWidth(160);

        topBar.getChildren().addAll(brand, typeBadge, spacer, timerLabel, progressLabel, progressBar);
        root.setTop(topBar);

        startCountdownTimer();

        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");

        Label paletteTitle = new Label("Question Palette");
        paletteTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
        paletteTitle.setTextFill(Color.web("#334155"));

        questionPalette = new FlowPane(8, 8);
        paletteButtons.clear();
        for (int i = 0; i < questions.size(); i++) {
            final int targetIndex = i;
            Button btn = new Button("Q" + (i + 1));
            btn.setPrefSize(42, 34);
            btn.setFont(Font.font("SansSerif", FontWeight.BOLD, 11));
            btn.setOnAction(e -> {
                saveCurrentAnswer();
                currentIndex = targetIndex;
                renderCurrentQuestion();
            });
            paletteButtons.add(btn);
            questionPalette.getChildren().add(btn);
        }

        sidebar.getChildren().addAll(paletteTitle, questionPalette);
        root.setLeft(sidebar);

        questionBox = new VBox(18);
        questionBox.setPadding(new Insets(28, 48, 28, 48));
        questionBox.setAlignment(Pos.TOP_LEFT);

        root.setCenter(questionBox);
        renderCurrentQuestion();

        return root;
    }

    private void startCountdownTimer() {
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                Platform.runLater(() -> {
                    mainApp.showAlert(Alert.AlertType.INFORMATION, "Time Expired", "Time has expired! Submitting your assessment automatically.");
                    submitAssessmentAction(null);
                });
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
        if (remainingSeconds < 60) {
            timerLabel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 4 10; -fx-background-radius: 12;");
        } else {
            timerLabel.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-padding: 4 10; -fx-background-radius: 12;");
        }
    }

    private void updatePaletteStyles() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String ans = interview.getAnswer(q.getId());
            boolean hasAns = ans != null && !ans.trim().isEmpty();

            Button btn = paletteButtons.get(i);
            if (i == currentIndex) {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
            } else if (hasAns) {
                btn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #334155; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        }
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
        updatePaletteStyles();

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
        textLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 17));
        textLabel.setTextFill(Color.web("#0f172a"));
        textLabel.setWrapText(true);

        questionBox.getChildren().addAll(metaBox, textLabel);

        String savedAnswer = interview.getAnswer(q.getId());

        if (q instanceof MCQQuestion) {
            MCQQuestion mcq = (MCQQuestion) q;
            mcqGroup = new ToggleGroup();
            VBox optionsBox = new VBox(10);
            optionsBox.setPadding(new Insets(10, 0, 10, 0));

            List<String> options = mcq.getOptions();
            for (int i = 0; i < options.size(); i++) {
                String letter = String.valueOf((char) ('A' + i));
                RadioButton rb = new RadioButton(letter + ") " + options.get(i));
                rb.setToggleGroup(mcqGroup);
                rb.setUserData(letter);
                rb.setFont(Font.font("SansSerif", 13.5));

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

        submitBtn.setOnAction(e -> submitAssessmentAction(submitBtn));

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

    private void submitAssessmentAction(Button submitBtn) {
        saveCurrentAnswer();
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        if (submitBtn != null) {
            submitBtn.setDisable(true);
        }

        Task<Result> task = new Task<>() {
            @Override
            protected Result call() throws Exception {
                return interviewService.submitInterview(interview);
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            renderResultScreen(task.getValue());
        });

        task.setOnFailed(workerStateEvent -> {
            if (submitBtn != null) submitBtn.setDisable(false);
            mainApp.showAlert(Alert.AlertType.ERROR, "Submission Error", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void renderResultScreen(Result result) {
        root.setTop(null);
        root.setLeft(null);

        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 48, 32, 48));
        content.setAlignment(Pos.TOP_CENTER);

        VBox summaryCard = new VBox(14);
        summaryCard.setAlignment(Pos.CENTER);
        summaryCard.setPadding(new Insets(24));
        summaryCard.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        summaryCard.setMaxWidth(800);

        Label title = new Label("Assessment Completed Successfully!");
        title.setFont(Font.font("SansSerif", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Label scoreLabel = new Label(String.format("%.1f / %.1f Marks", result.getTotalScore(), result.getMaxScore()));
        scoreLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 28));
        scoreLabel.setTextFill(Color.web("#2563eb"));

        Label pctLabel = new Label(String.format("Final Percentage: %.1f%%", result.getPercentage()));
        pctLabel.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 16));
        pctLabel.setTextFill(Color.web("#475569"));

        Label gradeBadge = new Label("Evaluation Grade: " + result.getGrade());
        gradeBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 6 16; -fx-background-radius: 16; -fx-font-size: 15px; -fx-font-weight: bold;");

        Button doneBtn = new Button("Return to Student Dashboard");
        doneBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
        doneBtn.setOnAction(e -> mainApp.showStudentScreen(student));

        summaryCard.getChildren().addAll(title, scoreLabel, pctLabel, gradeBadge, doneBtn);

        Label reviewHeader = new Label("Question-by-Question Review & Correct Answers");
        reviewHeader.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));
        reviewHeader.setTextFill(Color.web("#0f172a"));

        VBox reviewList = new VBox(16);
        reviewList.setMaxWidth(800);

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String candidateAns = interview.getAnswer(q.getId());
            double marksAwarded = interview.getMarks(q.getId());

            VBox card = new VBox(10);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");

            HBox cardHeader = new HBox(12);
            cardHeader.setAlignment(Pos.CENTER_LEFT);

            Label qNum = new Label("Q" + (i + 1) + " (" + q.getType() + " • " + q.getTopic() + ")");
            qNum.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
            qNum.setTextFill(Color.web("#1e40af"));

            Region cardSpacer = new Region();
            HBox.setHgrow(cardSpacer, Priority.ALWAYS);

            Label marksBadge = new Label(String.format("%.1f / %.1f Marks", marksAwarded, (double) q.getMaxMarks()));
            if (marksAwarded >= q.getMaxMarks()) {
                marksBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else if (marksAwarded > 0) {
                marksBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                marksBadge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
            }

            cardHeader.getChildren().addAll(qNum, cardSpacer, marksBadge);

            Label qText = new Label(q.getText());
            qText.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 13.5));
            qText.setWrapText(true);

            VBox yourAnsBox = new VBox(4);
            Label yourAnsTitle = new Label("Your Submitted Answer:");
            yourAnsTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 11));
            yourAnsTitle.setTextFill(Color.web("#475569"));
            Label yourAnsContent = new Label(candidateAns != null && !candidateAns.isBlank() ? candidateAns : "(No answer provided)");
            yourAnsContent.setWrapText(true);
            yourAnsContent.setStyle("-fx-background-color: #f8fafc; -fx-padding: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 4;");
            yourAnsBox.getChildren().addAll(yourAnsTitle, yourAnsContent);

            VBox correctAnsBox = new VBox(4);
            Label correctAnsTitle = new Label("Correct / Expected Answer:");
            correctAnsTitle.setFont(Font.font("SansSerif", FontWeight.BOLD, 11));
            correctAnsTitle.setTextFill(Color.web("#065f46"));

            Label correctAnsContent = new Label();
            correctAnsContent.setWrapText(true);
            correctAnsContent.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #065f46; -fx-padding: 8; -fx-border-color: #a7f3d0; -fx-border-radius: 4; -fx-font-weight: bold;");

            if (q instanceof MCQQuestion) {
                MCQQuestion mcq = (MCQQuestion) q;
                String correctText = mcq.getOptions().get(mcq.getCorrectIndex());
                correctAnsContent.setText(mcq.getCorrectLetter() + ") " + correctText);
            } else if (q instanceof KeywordBased) {
                List<String> kw = ((KeywordBased) q).getKeywords();
                correctAnsContent.setText("Key concepts & solution keywords expected: " + String.join(", ", kw));
            } else {
                correctAnsContent.setText("Standard solution based on assessment criteria.");
            }
            correctAnsBox.getChildren().addAll(correctAnsTitle, correctAnsContent);

            card.getChildren().addAll(cardHeader, qText, yourAnsBox, correctAnsBox);
            reviewList.getChildren().add(card);
        }

        content.getChildren().addAll(summaryCard, reviewHeader, reviewList);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.setCenter(scroll);
    }
}

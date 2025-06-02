package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import logic.enums.AcademicType;
import logic.logicclasses.Academic;

public class AcademicMenuWindow {
    private final BorderPane root;
    private final TabPane tabPane;
    private Button logoutButton;
    private Button registerStudentButton;
    private Button consultStudentsButton;
    private Button manageStudentButton;
    private Button registerFinalGradeButton;
    private Button consultPresentationEvaluationsButton;
    private Button registerPartialEvaluationButton;
    private Button consultPartialEvaluationsButton;

    private final String BLUE_DARK_COLOR = "#0A1F3F";
    private final String GREEN_DARK_COLOR = "#1A5F4B";
    private final String WHITE_COLOR = "#FFFFFF";
    private final String LIGHT_GREY_COLOR = "#F8F9FA";

    public AcademicMenuWindow(Academic academic) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_GREY_COLOR + ";");

        root.setTop(createHeader(academic));

        tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        styleTabPane();

        if (academic.getAcademicType() == AcademicType.EE) {
            Tab studentsTab = createTab("Estudiantes", createStudentsContent());
            Tab evaluationsTab = createTab("Evaluaciones", createEEContent());
            tabPane.getTabs().addAll(studentsTab, evaluationsTab);
        } else if (academic.getAcademicType() == AcademicType.Evaluador) {
            Tab evaluationsTab = createTab("Evaluaciones", createEvaluatorContent());
            tabPane.getTabs().add(evaluationsTab);
        }

        root.setCenter(tabPane);
    }

    private void styleTabPane() {
        tabPane.setStyle("-fx-background-color: " + BLUE_DARK_COLOR + ";");
        tabPane.setPrefWidth(200);
    }

    private HBox createHeader(Academic academic) {
        Image logo = new Image(getClass().getResource("/images/uvLogo.png").toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(60);
        logoView.setPreserveRatio(true);

        Label lblName = new Label(academic.getFullName());
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblName.setTextFill(Color.web(WHITE_COLOR));

        Label lblType = new Label(academic.getAcademicType().toString());
        lblType.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        lblType.setTextFill(Color.web(WHITE_COLOR));

        logoutButton = new Button("Cerrar Sesión");
        logoutButton.setStyle("-fx-background-color: RED; " +
                "-fx-text-fill: " + WHITE_COLOR + "; " +
                "-fx-font-weight: bold; " +
                "-fx-border-color: " + WHITE_COLOR + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 3;");

        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-text-fill: " + WHITE_COLOR + "; " +
                "-fx-border-color: " + WHITE_COLOR + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 3;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: RED; " +
                "-fx-text-fill: " + WHITE_COLOR + "; " +
                "-fx-border-color: " + WHITE_COLOR + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 3;"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userInfo = new VBox(lblName, lblType);
        userInfo.setSpacing(5);

        HBox header = new HBox(20, logoView, userInfo, spacer, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + BLUE_DARK_COLOR + "; " +
                "-fx-padding: 15 20; " +
                "-fx-border-width: 0 0 3 0; " +
                "-fx-border-color: " + GREEN_DARK_COLOR + ";");
        return header;
    }

    private Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title);
        tab.setClosable(false);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + LIGHT_GREY_COLOR + ";");

        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createStudentsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerStudentButton = createStyledButton("Registrar Estudiante");
        consultStudentsButton = createStyledButton("Consultar Estudiantes");

        content.getChildren().addAll(
                createSectionTitle("Gestión de Estudiantes"),
                registerStudentButton,
                consultStudentsButton
        );

        return content;
    }

    private VBox createEEContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerFinalGradeButton = createStyledButton("Registrar Calificación Final");
        consultPresentationEvaluationsButton = createStyledButton("Consultar Evaluaciones de Presentaciones");

        content.getChildren().addAll(
                createSectionTitle("Evaluaciones"),
                registerFinalGradeButton,
                consultPresentationEvaluationsButton
        );

        return content;
    }

    private VBox createEvaluatorContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerPartialEvaluationButton = createStyledButton("Registrar Evaluación Parcial");
        consultPartialEvaluationsButton = createStyledButton("Consultar Evaluaciones Parciales");

        content.getChildren().addAll(
                createSectionTitle("Evaluaciones Parciales"),
                registerPartialEvaluationButton,
                consultPartialEvaluationsButton
        );

        return content;
    }

    private Label createSectionTitle(String text) {
        Label title = new Label(text);
        title.setStyle("-fx-font-size: 16; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: " + BLUE_DARK_COLOR + ";");
        return title;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + BLUE_DARK_COLOR + "; " +
                "-fx-text-fill: " + WHITE_COLOR + "; " +
                "-fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10 15; " +
                "-fx-min-width: 250px;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + GREEN_DARK_COLOR + "; " +
                "-fx-text-fill: " + WHITE_COLOR + "; " +
                "-fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10 15; " +
                "-fx-min-width: 250px;"));

        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + BLUE_DARK_COLOR + "; " +
                "-fx-text-fill: " + WHITE_COLOR + "; " +
                "-fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10 15; " +
                "-fx-min-width: 250px;"));
        return btn;
    }

    public BorderPane getView() {
        return root;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }

    public Button getRegisterStudentButton() {
        return registerStudentButton;
    }

    public Button getConsultStudentsButton() {
        return consultStudentsButton;
    }

    public Button getManageStudentButton() {
        return manageStudentButton;
    }

    public Button getRegisterFinalGradeButton() {
        return registerFinalGradeButton;
    }

    public Button getConsultPresentationEvaluationsButton() {
        return consultPresentationEvaluationsButton;
    }

    public Button getRegisterPartialEvaluationButton() {
        return registerPartialEvaluationButton;
    }

    public Button getConsultPartialEvaluationsButton() {
        return consultPartialEvaluationsButton;
    }
}
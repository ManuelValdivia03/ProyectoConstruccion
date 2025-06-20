package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import logic.logicclasses.Student;

public class StudentMenuWindow {
    private final BorderPane root;
    private final TabPane tabPane;
    private Button logoutButton;
    private Button profileButton;
    private Button requestProjectButton;
    private Button viewAssignedProjectButton;
    private Button viewScheduleButton;
    private Button registerSelfEvaluationButton;
    private Button registReportButton;
    private Button viewEvaluationsButton;

    private final String BLUE_DARK_COLOR = "#0A1F3F";
    private final String GREEN_DARK_COLOR = "#1A5F4B";
    private final String WHITE_COLOR = "#FFFFFF";
    private final String LIGHT_GREY_COLOR = "#F8F9FA";

    public StudentMenuWindow(Student student) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_GREY_COLOR + ";");

        root.setTop(createHeader(student));

        tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        styleTabPane();

        Tab projectTab = createTab("Proyectos", createProjectContent());
        Tab activitiesTab = createTab("Actividades", createActivitiesContent());
        Tab evaluationsTab = createTab("Evaluaciones", createEvaluationsContent());

        tabPane.getTabs().addAll(projectTab, activitiesTab, evaluationsTab);
        root.setCenter(tabPane);
    }

    private void styleTabPane() {
        tabPane.setStyle("-fx-background-color: " + BLUE_DARK_COLOR + ";");
        tabPane.setPrefWidth(200);
    }

    private HBox createHeader(Student student) {
        Image logo = new Image(getClass().getResource("/images/uvLogo.png").toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(60);
        logoView.setPreserveRatio(true);

        Label lblName = new Label(student.getFullName());
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblName.setTextFill(Color.web(WHITE_COLOR));

        Image profileImage = new Image(getClass().getResource("/images/profile.png").toExternalForm());
        ImageView profileView = new ImageView(profileImage);
        profileView.setFitWidth(30);
        profileView.setPreserveRatio(true);
        profileButton = new Button();
        profileButton.setGraphic(profileView);
        profileButton.setStyle("-fx-background-color: transparent;");

        profileButton.setOnMouseEntered(e -> profileButton.setStyle("-fx-background-color: rgba(255,255,255,0.1);"));
        profileButton.setOnMouseExited(e -> profileButton.setStyle("-fx-background-color: transparent;"));

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

        HBox header = new HBox(20, logoView, lblName, spacer, profileButton,logoutButton);
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

    private VBox createProjectContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        requestProjectButton = createStyledButton("Registrar Solicitud de Proyecto");
        viewAssignedProjectButton = createStyledButton("Consultar Proyecto Asignado");

        content.getChildren().addAll(
                createSectionTitle("Gestión de Proyectos"),
                requestProjectButton,
                viewAssignedProjectButton
        );

        return content;
    }

    private VBox createActivitiesContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        viewScheduleButton = createStyledButton("Consultar Cronograma de Actividades");
        registReportButton = createStyledButton("Registrar Reportes");

        content.getChildren().addAll(
                createSectionTitle("Actividades del Proyecto"),
                viewScheduleButton,
                registReportButton
        );

        return content;
    }

    private VBox createEvaluationsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerSelfEvaluationButton = createStyledButton("Registrar Autoevaluación");
        viewEvaluationsButton = createStyledButton("Consultar Evaluaciones");

        content.getChildren().addAll(
                createSectionTitle("Evaluaciones"),
                registerSelfEvaluationButton,
                viewEvaluationsButton
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

    public Button getRequestProjectButton() {
        return requestProjectButton;
    }

    public Button getViewAssignedProjectButton() {
        return viewAssignedProjectButton;
    }

    public Button getViewScheduleButton() {
        return viewScheduleButton;
    }

    public Button getRegisterSelfEvaluationButton() {
        return registerSelfEvaluationButton;
    }

    public Button getRegistReportButton() {
        return registReportButton;
    }

    public Button getViewEvaluationsButton() {
        return viewEvaluationsButton;
    }

    public Button getProfileButton() {
        return profileButton;
    }
}
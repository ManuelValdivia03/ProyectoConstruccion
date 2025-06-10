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
import logic.logicclasses.Coordinator;

public class CoordinatorMenuWindow {
    private final BorderPane root;
    private final TabPane tabPane;
    private Button logoutButton;
    private Button registerAcademicButton;
    private Button consultAcademicButton;
    private Button registerProjectButton;
    private Button consultProjectButton;
    private Button assignProjectButton;
    private Button reassignStudentButton;
    private Button manageRequestsButton;
    private Button registerCronogramButton;
    private Button enableEvaluationsButton;
    private Button registerOrganizationButton;
    private Button consultOrganizationButton;
    private Button registerRepresentativeButton;
    private Button consultRepresentativesButton;
    private Button generateStadisticsButton;

    private final String BLUE_DARK_COLOR = "#0A1F3F";
    private final String GREEN_DARK_COLOR= "#1A5F4B";
    private final String WHITE_COLOR = "#FFFFFF";
    private final String LIGHT_GREY_COLOR = "#F8F9FA";

    public CoordinatorMenuWindow(Coordinator coordinator) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_GREY_COLOR + ";");

        root.setTop(createHeader(coordinator));

        tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        styleTabPane();

        Tab academicTab = createTab("Académicos", createAcademicContent());
        Tab projectTab = createTab("Proyectos", createProjectContent());
        Tab orgTab = createTab("Organizaciones", createOrganizationContent());
        Tab stadisticsTab = createTab("Estadísticas", createStatisticsContent());

        tabPane.getTabs().addAll(academicTab, projectTab, orgTab, stadisticsTab);
        root.setCenter(tabPane);
    }

    private void styleTabPane() {
        tabPane.setStyle("-fx-background-color: " + BLUE_DARK_COLOR + ";");
        tabPane.setPrefWidth(200);
    }

    private HBox createHeader(Coordinator coordinator) {
        Image logo = new Image(getClass().getResource("/images/uvLogo.png").toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(60);
        logoView.setPreserveRatio(true);

        Label lblName = new Label(coordinator.getFullName());
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblName.setTextFill(Color.web(WHITE_COLOR));

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

        HBox header = new HBox(20, logoView, lblName, spacer, logoutButton);
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

    private VBox createAcademicContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerAcademicButton = createStyledButton("Registrar académico");
        consultAcademicButton = createStyledButton("Consultar académicos");

        content.getChildren().addAll(
                createSectionTitle("Gestión de Académicos"),
                registerAcademicButton,
                consultAcademicButton
        );

        return content;
    }

    private VBox createProjectContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerProjectButton = createStyledButton("Registrar Proyecto");
        consultProjectButton = createStyledButton("Consultar Proyectos");
        assignProjectButton = createStyledButton("Asignar Proyecto");
        reassignStudentButton = createStyledButton("Reasignar Estudiante");
        manageRequestsButton = createStyledButton("Gestionar Solicitudes");
        registerCronogramButton = createStyledButton("Registrar Cronograma");
        enableEvaluationsButton = createStyledButton("Habilitar Evaluaciones");

        content.getChildren().addAll(
                createSectionTitle("Gestión de Proyectos"),
                registerProjectButton,
                consultProjectButton,
                assignProjectButton,
                reassignStudentButton,
                manageRequestsButton,
                registerCronogramButton,
                enableEvaluationsButton
        );
        return content;
    }

    private VBox createOrganizationContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        registerOrganizationButton = createStyledButton("Registrar Organización");
        consultOrganizationButton = createStyledButton("Consultar Organizaciones");
        registerRepresentativeButton = createStyledButton("Registrar Representante");
        consultRepresentativesButton = createStyledButton("Consultar Representantes");

        content.getChildren().addAll(
                createSectionTitle("Organizaciones Vinculadas"),
                registerOrganizationButton,
                consultOrganizationButton,
                registerRepresentativeButton,
                consultRepresentativesButton
        );

        return content;
    }

    private VBox createStatisticsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        generateStadisticsButton = createStyledButton("Generar Estadísticas");

        content.getChildren().addAll(
                createSectionTitle("Estadísticas Académicas"),
                generateStadisticsButton
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

    public Button getRegisterAcademicButton() {
        return registerAcademicButton;
    }

    public Button getConsultAcademicButton() {
        return consultAcademicButton;
    }

    public Button getRegisterProjectButton() {
        return registerProjectButton;
    }

    public Button getConsultProjectButton() {
        return consultProjectButton;
    }

    public Button getAssignProjectButton() {
        return assignProjectButton;
    }

    public Button getReassignStudentButton() {
        return reassignStudentButton;
    }

    public Button getManageRequestsButton() {
        return manageRequestsButton;
    }

    public Button getRegisterCronogramButton() {
        return registerCronogramButton;
    }

    public Button getEnableEvaluationsButton() {
        return enableEvaluationsButton;
    }

    public Button getRegisterOrganizationButton() {
        return registerOrganizationButton;
    }

    public Button getConsultOrganizationButton() {
        return consultOrganizationButton;
    }

    public Button getRegisterRepresentativeButton() {
        return registerRepresentativeButton;
    }

    public Button getConsultRepresentativesButton() {
        return consultRepresentativesButton;
    }

    public Button getGenerateStadisticsButton() {
        return generateStadisticsButton;
    }


}

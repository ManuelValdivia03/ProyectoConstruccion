package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.logicclasses.Coordinator;
import userinterface.windows.*;

public class ControllerCoordinatorMenuWindow implements EventHandler<ActionEvent> {
    private static final int MAIN_WINDOW_WIDTH = 1024;
    private static final int MAIN_WINDOW_HEIGHT = 768;

    private final CoordinatorMenuWindow view;
    private final Stage primaryStage;
    private final Runnable onLogout;

    public ControllerCoordinatorMenuWindow(Stage primaryStage, Coordinator coordinator, Runnable onLogout) {
        this.primaryStage = primaryStage;
        this.onLogout = onLogout;
        this.view = new CoordinatorMenuWindow(coordinator);

        setupEventHandlers();
        initializeStage();
    }

    private void setupEventHandlers() {
        view.getLogoutButton().setOnAction(this::handleLogout);
        view.getRegisterAcademicButton().setOnAction(e -> showAcademicRegistrationWindow());
        view.getConsultAcademicButton().setOnAction(e -> showConsultAcademicsWindow());
        view.getRegisterProjectButton().setOnAction(e -> showRegisterProjectWindow());
        view.getConsultProjectButton().setOnAction(e -> showConsultProjectsWindow());
        view.getRegisterOrganizationButton().setOnAction(e -> showRegisterOrganizationWindow());
        view.getConsultOrganizationButton().setOnAction(e -> showConsultOrganizationWindow());
        view.getRegisterRepresentativeButton().setOnAction(e -> showRegisterRepresentativeWindow());
        view.getConsultRepresentativesButton().setOnAction(e -> showConsultRepresentativesWindow());
        view.getRegisterGroupButton().setOnAction(e -> showRegisterGroupWindow());
        view.getConsultGroupsButton().setOnAction(e -> showConsultGroupsWindow());
        view.getAssignProjectButton().setOnAction(e -> showAssignProjectWindow());
        view.getReassignStudentButton().setOnAction(e -> showReassignProjectWindow());
        view.getRegisterCronogramButton().setOnAction(e -> showRegisterCronogramWindow());
        view.getEnableEvaluationsButton().setOnAction(this::handleUnimplementedAction);
        view.getGenerateStadisticsButton().setOnAction(e -> showStatisticsWindow());
    }

    private void handleLogout(ActionEvent event) {
        onLogout.run();
        primaryStage.close();
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleUnimplementedAction(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Funcionalidad no implementada",
                "Esta funcionalidad aún no está implementada. Por favor, inténtelo más tarde.");
    }

    private void showAcademicRegistrationWindow() {
        CreateAcademicWindow academicWindow = new CreateAcademicWindow();
        new ControllerCreateAcademicWindow(academicWindow);
        showWindow("Registro de Académico", academicWindow.getView(), 800, 500);
    }

    private void showConsultAcademicsWindow() {
        ConsultAcademicsWindow consultWindow = new ConsultAcademicsWindow();
        Stage consultStage = new Stage();
        new ControllerConsultAcademicsWindow(consultWindow, consultStage);
        showWindow("Consulta de Académicos", consultWindow.getView(), 980, 600, consultStage);
    }

    private void showRegisterProjectWindow() {
        RegistProyectWindow registerWindow = new RegistProyectWindow();
        Stage registerStage = new Stage();
        new ControllerRegistProyectWindow(registerWindow, registerStage);
        showWindow("Registrar proyecto", registerWindow.getView(), 500, 400, registerStage);
    }

    private void showConsultProjectsWindow() {
        ConsultProyectsWindow consultWindow = new ConsultProyectsWindow();
        new ControllerConsultProyectsWindow(consultWindow);
        showWindow("Consulta de Proyectos", consultWindow.getView(), 1000, 600);
    }

    private void showRegisterOrganizationWindow() {
        CreateLinkedOrganizationWindow registerWindow = new CreateLinkedOrganizationWindow();
        new ControllerCreateLinkedOrganizationWindow(registerWindow);
        showWindow("Registrar Organización", registerWindow.getView(), 800, 400);
    }

    private void showConsultOrganizationWindow() {
        ConsultLinkedOrganizationsWindow consultWindow = new ConsultLinkedOrganizationsWindow();
        Stage consultStage = new Stage();
        new ControllerConsultLinkedOrganizationsWindow(consultWindow, consultStage);
        showWindow("Consulta de Organizaciones", consultWindow.getView(), 1200, 600, consultStage);
    }

    private void showRegisterRepresentativeWindow() {
        RegistRepresentativeWindow registerWindow = new RegistRepresentativeWindow();
        Stage registerStage = new Stage();
        new ControllerRegistRepresentativeWindow(registerWindow, registerStage);
        showWindow("Registrar Representante", registerWindow.getView(), 500, 400, registerStage);
    }

    private void showConsultRepresentativesWindow() {
        ConsultRepresentativesWindow consultWindow = new ConsultRepresentativesWindow();
        Stage consultStage = new Stage();
        new ControllerConsultRepresentativesWindow(consultWindow, consultStage);
        showWindow("Consultar Representantes", consultWindow.getView(), 900, 500, consultStage);
    }

    private void showRegisterGroupWindow() {
        CreateGroupWindow groupWindow = new CreateGroupWindow();
        new ControllerCreateGroupWindow(groupWindow);
        showWindow("Registrar Grupo", groupWindow.getView(), 400, 300);
    }

    private void showConsultGroupsWindow() {
        ConsultGroupsWindow consultWindow = new ConsultGroupsWindow();
        Stage consultStage = new Stage();
        new ControllerConsultGroupsWindow(consultWindow, consultStage);
        showWindow("Consulta de Grupos", consultWindow.getView(), 600, 400, consultStage);
    }

    private void showAssignProjectWindow() {
        CoordinatorProjectsWindow assignWindow = new CoordinatorProjectsWindow();
        Stage assignStage = new Stage();
        new ControllerCoordinatorProjectsWindow(assignWindow);
        showWindow("Asignar Proyecto", assignWindow.getView(), 600, 400, assignStage);
    }

    private void showReassignProjectWindow() {
        Stage reassignStage = new Stage();
        ControllerReassignProjectWindow controller = new ControllerReassignProjectWindow(reassignStage);
        controller.show();
    }

    private void showRegisterCronogramWindow() {
        Stage cronogramStage = new Stage();
        ControllerRegistCronogramActivityWindow controller = new ControllerRegistCronogramActivityWindow(cronogramStage);
        controller.show();
    }

    private void showStatisticsWindow() {
        ControllerStatisticsWindow controller = new ControllerStatisticsWindow(primaryStage, null);
        controller.show();
    }

    private void showWindow(String title, javafx.scene.Parent view, int width, int height) {
        showWindow(title, view, width, height, new Stage());
    }

    private void showWindow(String title, javafx.scene.Parent view, int width, int height, Stage stage) {
        stage.setScene(new Scene(view, width, height));
        stage.setTitle(title);
        stage.show();
    }

    private void initializeStage() {
        primaryStage.setScene(new Scene(view.getView(), MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));
        primaryStage.setTitle("Sistema de Coordinación - Universidad");
        primaryStage.show();
    }

    @Override
    public void handle(ActionEvent event) {
    }
}

package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.logicclasses.Coordinator;
import userinterface.windows.*;

public class ControllerCoordinatorMenuWindow implements EventHandler<ActionEvent> {
    private final CoordinatorMenuWindow view;
    private final Stage stage;
    private final Runnable onLogout;

    public ControllerCoordinatorMenuWindow(Stage stage, Coordinator coordinator, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        this.view = new CoordinatorMenuWindow(coordinator);

        setupEventHandlers();
        initializeStage();
    }

    private void setupEventHandlers() {
        view.getLogoutButton().setOnAction(e -> {
            onLogout.run();
            stage.close();
        });

        view.getRegisterAcademicButton().setOnAction(e -> showAcademicRegistrationWindow());
        view.getConsultAcademicButton().setOnAction(e -> showConsultAcademicsWindow());
        view.getRegisterProjectButton().setOnAction(e -> showRegisterProjectWindow());
        view.getConsultProjectButton().setOnAction(e -> showConsultProjectsWindow());
        view.getAssignProjectButton().setOnAction(e -> {});
        view.getReassignStudentButton().setOnAction(e -> {});
        view.getManageRequestsButton().setOnAction(e -> {});
        view.getRegisterCronogramButton().setOnAction(e -> {});
        view.getEnableEvaluationsButton().setOnAction(e -> {});
        view.getRegisterOrganizationButton().setOnAction(e -> showRegisterOrganizationWindow());
        view.getConsultOrganizationButton().setOnAction(e -> showConsultOrganizationWindow());
        view.getGenerateStadisticsButton().setOnAction(e -> {});
    }

    private void showAcademicRegistrationWindow() {
        Stage academicStage = new Stage();
        CreateAcademicWindow academicWindow = new CreateAcademicWindow();
        new ControllerCreateAcademicWindow(academicWindow);

        academicStage.setScene(new Scene(academicWindow.getView(), 500, 500));
        academicStage.setTitle("Registro de Académico");
        academicStage.show();
    }

    private void showConsultAcademicsWindow() {
        Stage consultStage = new Stage();
        ConsultAcademicsWindow consultWindow = new ConsultAcademicsWindow();
        new ControllerConsultAcademicsWindow(consultWindow, consultStage);

        consultStage.setScene(new Scene(consultWindow.getView(), 800, 600));
        consultStage.setTitle("Consulta de Académicos");
        consultStage.show();
    }

    private void showRegisterProjectWindow() {
        Stage registerStage = new Stage();
        RegistProyectWindow registerWindow = new RegistProyectWindow();
        new ControllerRegistProyectWindow(registerWindow);

        registerStage.setScene(new Scene(registerWindow.getView(), 500, 400));
        registerStage.setTitle("Registrar proyecto");
        registerStage.show();
    }

    private void showConsultProjectsWindow() {
        Stage consultStage = new Stage();
        ConsultProyectsWindow consultWindow = new ConsultProyectsWindow();
        new ControllerConsultProyectsWindow(consultWindow);

        consultStage.setScene(new Scene(consultWindow.getView(), 800, 600));
        consultStage.setTitle("Consulta de Proyectos");
        consultStage.show();
    }

    private void showRegisterOrganizationWindow() {
        Stage registerStage = new Stage();
        CreateLinkedOrganizationWindow registerWindow = new CreateLinkedOrganizationWindow();
        new ControllerCreateLinkedOrganizationWindow(registerWindow);

        registerStage.setScene(new Scene(registerWindow.getView(), 500, 400));
        registerStage.setTitle("Registrar Organización");
        registerStage.show();
    }

    private void showConsultOrganizationWindow() {
        Stage consultStage = new Stage();
        ConsultLinkedOrganizationsWindow consultWindow = new ConsultLinkedOrganizationsWindow();
        new ControllerConsultLinkedOrganizationsWindow(consultWindow, consultStage);

        consultStage.setScene(new Scene(consultWindow.getView(), 800, 600));
        consultStage.setTitle("Consulta de Organizaciones");
        consultStage.show();
    }

    private void initializeStage() {
        stage.setScene(new Scene(view.getView(), 1024, 768));
        stage.setTitle("Sistema de Coordinación - Universidad");
        stage.show();
    }

    @Override
    public void handle(ActionEvent event) {
        // Manejo de eventos específicos
    }
}
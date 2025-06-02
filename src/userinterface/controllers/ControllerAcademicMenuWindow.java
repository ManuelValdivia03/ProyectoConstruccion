package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.enums.AcademicType;
import logic.logicclasses.Academic;
import main.RegisterStudentTest;
import userinterface.windows.*;

public class ControllerAcademicMenuWindow implements EventHandler<ActionEvent> {
    private final AcademicMenuWindow view;
    private final Stage stage;
    private final Runnable onLogout;

    public ControllerAcademicMenuWindow(Stage stage, Academic academic, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        this.view = new AcademicMenuWindow(academic);

        setupEventHandlers(academic.getAcademicType());
        initializeStage();
    }

    private void setupEventHandlers(AcademicType type) {
        view.getLogoutButton().setOnAction(e -> {
            onLogout.run();
            stage.close();
        });

        if (type == AcademicType.EE) {
            view.getRegisterStudentButton().setOnAction(e -> showRegisterStudentWindow());
            view.getConsultStudentsButton().setOnAction(e -> showConsultStudentsWindow());
            view.getRegisterFinalGradeButton().setOnAction(e -> showRegisterFinalGradeWindow());
            view.getConsultPresentationEvaluationsButton().setOnAction(e -> showPresentationEvaluationsWindow());
        } else if (type == AcademicType.Evaluador) {
            view.getRegisterPartialEvaluationButton().setOnAction(e -> showRegisterPartialEvaluationWindow());
            view.getConsultPartialEvaluationsButton().setOnAction(e -> showConsultPartialEvaluationsWindow());
        }
    }

    private void showRegisterStudentWindow() {
        Stage registerStage = new Stage();
        CreateStudentWindow registerWindow = new CreateStudentWindow();
        new ControllerCreateStudentWindow(registerWindow);

        registerStage.setScene(new Scene(registerWindow.getView(), 500, 400));
        registerStage.setTitle("Registrar Estudiante");
        registerStage.show();
    }

    private void showConsultStudentsWindow() {
        Stage consultStage = new Stage();
        ConsultStudentsWindow consultWindow = new ConsultStudentsWindow();
        new ControllerConsultStudentsWindow(consultWindow, consultStage);

        consultStage.setScene(new Scene(consultWindow.getView(), 800, 600));
        consultStage.setTitle("Consultar Estudiantes");
        consultStage.show();
    }

private void showRegisterFinalGradeWindow() {
        Stage consultStage = new Stage();
        ConsultStudentsWindow consultStudentsWindow = new ConsultStudentsWindow();
        new ControllerConsultStudentsWindow(consultStudentsWindow, consultStage);

        consultStage.setScene(new Scene(consultStudentsWindow.getView(), 500, 400));
        consultStage.setTitle("Registrar Nota Final");
        consultStage.show();
    }

    private void showPresentationEvaluationsWindow() {
    }

    private void showRegisterPartialEvaluationWindow() {
    }

    private void showConsultPartialEvaluationsWindow() {
    }

    private void initializeStage() {
        stage.setScene(new Scene(view.getView(), 1024, 768));
        stage.setTitle("Sistema de Académicos - Universidad");
        stage.show();
    }

    @Override
    public void handle(ActionEvent event) {
        // Manejo de eventos específicos
    }
}
package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.logicclasses.Student;
import userinterface.windows.*;

public class ControllerStudentMenuWindow implements EventHandler<ActionEvent> {
    private final StudentMenuWindow view;
    private final Stage stage;
    private final Runnable onLogout;
    private final Student student;

    public ControllerStudentMenuWindow(Stage stage, Student student, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        this.student = student;
        this.view = new StudentMenuWindow(student);

        setupEventHandlers();
        initializeStage();
    }

    private void setupEventHandlers() {
        view.getLogoutButton().setOnAction(e -> {
            onLogout.run();
            stage.close();
        });

        view.getProfileButton().setOnAction(e -> showAccountUpdateWindow());

        view.getRequestProjectButton().setOnAction(e -> showProjectRequestWindow());
        view.getViewAssignedProjectButton().setOnAction(e -> showAssignedProjectWindow());
        view.getViewScheduleButton().setOnAction(e -> showScheduleWindow());
        view.getRegisterSelfEvaluationButton().setOnAction(e -> showSelfEvaluationWindow());
        view.getRegisterMonthlyReportButton().setOnAction(e -> showMonthlyReportWindow());
        view.getViewEvaluationsButton().setOnAction(e -> showEvaluationsWindow());
    }

    private void showProjectRequestWindow() {
    }

    private void showAssignedProjectWindow() {
    }

    private void showScheduleWindow() {
    }

    private void showSelfEvaluationWindow() {
    }

    private void showMonthlyReportWindow() {
    }

    private void showEvaluationsWindow() {
    }

    private void showAccountUpdateWindow() {
        EditProfileStudentWindow editProfileStudent = new EditProfileStudentWindow();
        Stage accountStage = new Stage();
        new ControllerEditProfileStudentWindow(
                editProfileStudent,
                student,
                accountStage,
                () -> {
                }
        );

        accountStage.setScene(new Scene(editProfileStudent.getView(), 600, 400));
        accountStage.setTitle("Actualizar Cuenta");
        accountStage.show();
    }

    private void initializeStage() {
        stage.setScene(new Scene(view.getView(), 1024, 768));
        stage.setTitle("Sistema de Estudiantes - Universidad");
        stage.show();
    }

    @Override
    public void handle(ActionEvent event) {
        // Manejo de eventos específicos
    }
}
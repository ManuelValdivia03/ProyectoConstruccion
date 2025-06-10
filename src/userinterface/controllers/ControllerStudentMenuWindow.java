package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.logicclasses.Student;
import userinterface.windows.EditProfileStudentWindow;
import userinterface.windows.StudentMenuWindow;

public class ControllerStudentMenuWindow {
    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    private static final int PROFILE_WINDOW_WIDTH = 600;
    private static final int PROFILE_WINDOW_HEIGHT = 400;

    private final StudentMenuWindow view;
    private final Stage primaryStage;
    private final Runnable logoutHandler;
    private final Student student;

    public ControllerStudentMenuWindow(Stage primaryStage, Student student, Runnable logoutHandler) {
        this.primaryStage = primaryStage;
        this.student = student;
        this.logoutHandler = logoutHandler;
        this.view = new StudentMenuWindow(student);

        initializeView();
        setupEventHandlers();
    }

    private void initializeView() {
        Scene scene = new Scene(view.getView(), WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Estudiantes - Universidad");
        primaryStage.show();
    }

    private void setupEventHandlers() {
        view.getLogoutButton().setOnAction(this::handleLogout);
        view.getProfileButton().setOnAction(this::handleProfileButton);
        view.getRequestProjectButton().setOnAction(this::handleProjectRequest);
        view.getViewAssignedProjectButton().setOnAction(this::handleAssignedProject);
        view.getViewScheduleButton().setOnAction(this::handleSchedule);
        view.getRegisterSelfEvaluationButton().setOnAction(this::handleSelfEvaluation);
        view.getRegisterMonthlyReportButton().setOnAction(this::handleMonthlyReport);
        view.getViewEvaluationsButton().setOnAction(this::handleEvaluations);
    }

    private void handleLogout(ActionEvent event) {
        logoutHandler.run();
        primaryStage.close();
    }

    private void handleProfileButton(ActionEvent event) {
        showAccountUpdateWindow();
    }

    private void handleProjectRequest(ActionEvent event) {
        showProjectRequestWindow();
    }

    private void handleAssignedProject(ActionEvent event) {
        showAssignedProjectWindow();
    }

    private void handleSchedule(ActionEvent event) {
        showScheduleWindow();
    }

    private void handleSelfEvaluation(ActionEvent event) {
        showSelfEvaluationWindow();
    }

    private void handleMonthlyReport(ActionEvent event) {
        showMonthlyReportWindow();
    }

    private void handleEvaluations(ActionEvent event) {
        showEvaluationsWindow();
    }

    private void showAccountUpdateWindow() {
        EditProfileStudentWindow editProfileView = new EditProfileStudentWindow();
        Stage profileStage = new Stage();

        new ControllerEditProfileStudentWindow(
                editProfileView,
                student,
                profileStage,
                () -> {}
        );

        Scene profileScene = new Scene(editProfileView.getView(), PROFILE_WINDOW_WIDTH, PROFILE_WINDOW_HEIGHT);
        profileStage.setScene(profileScene);
        profileStage.setTitle("Actualizar Cuenta");
        profileStage.show();
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
}
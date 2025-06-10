package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.enums.AcademicType;
import logic.logicclasses.Academic;
import userinterface.windows.AcademicMenuWindow;
import userinterface.windows.CreateStudentWindow;
import userinterface.windows.ConsultStudentsWindow;
import java.util.Objects;

public class ControllerAcademicMenuWindow implements EventHandler<ActionEvent> {
    private static final int MAIN_WINDOW_WIDTH = 1024;
    private static final int MAIN_WINDOW_HEIGHT = 768;
    private static final int STUDENT_REGISTER_WIDTH = 500;
    private static final int STUDENT_REGISTER_HEIGHT = 400;
    private static final int STUDENT_CONSULT_WIDTH = 800;
    private static final int STUDENT_CONSULT_HEIGHT = 600;

    private final AcademicMenuWindow view;
    private final Stage stage;
    private final Runnable onLogout;

    public ControllerAcademicMenuWindow(Stage stage, Academic academic, Runnable onLogout) {
        this.stage = Objects.requireNonNull(stage, "Stage no puede ser nulo");
        this.onLogout = Objects.requireNonNull(onLogout, "Callback de logout no puede ser nulo");
        this.view = new AcademicMenuWindow(Objects.requireNonNull(academic, "Academic no puede ser nulo"));

        configureMainWindow();
        setupMenuForAcademicType(academic.getAcademicType());
    }

    private void configureMainWindow() {
        stage.setScene(new Scene(view.getView(), MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));
        stage.setTitle("Sistema de Académicos - Universidad");
        stage.show();
    }

    private void setupMenuForAcademicType(AcademicType type) {
        view.getLogoutButton().setOnAction(this::handleLogout);

        switch (type) {
            case EE:
                configureEEButtons();
                break;
            case Evaluador:
                configureEvaluatorButtons();
                break;
            default:
                throw new IllegalArgumentException("Tipo académico no soportado: " + type);
        }
    }

    private void configureEEButtons() {
        view.getRegisterStudentButton().setOnAction(e -> showRegisterStudentWindow());
        view.getConsultStudentsButton().setOnAction(e -> showConsultStudentsWindow());
        view.getRegisterFinalGradeButton().setOnAction(e -> showRegisterFinalGradeWindow());
        view.getConsultPresentationEvaluationsButton().setOnAction(e -> showNotImplementedMessage());
    }

    private void configureEvaluatorButtons() {
        view.getRegisterPartialEvaluationButton().setOnAction(e -> showNotImplementedMessage());
        view.getConsultPartialEvaluationsButton().setOnAction(e -> showNotImplementedMessage());
    }

    private void showRegisterStudentWindow() {
        Stage registerStage = new Stage();
        CreateStudentWindow registerWindow = new CreateStudentWindow();
        new ControllerCreateStudentWindow(registerWindow);

        configureAndShowWindow(registerStage, registerWindow.getView(),
                "Registrar Estudiante",
                STUDENT_REGISTER_WIDTH, STUDENT_REGISTER_HEIGHT);
    }

    private void showConsultStudentsWindow() {
        Stage consultStage = new Stage();
        ConsultStudentsWindow consultWindow = new ConsultStudentsWindow();
        new ControllerConsultStudentsWindow(consultWindow, consultStage);

        configureAndShowWindow(consultStage, consultWindow.getView(),
                "Consultar Estudiantes",
                STUDENT_CONSULT_WIDTH, STUDENT_CONSULT_HEIGHT);
    }

    private void showRegisterFinalGradeWindow() {
        Stage gradeStage = new Stage();
        ConsultStudentsWindow consultWindow = new ConsultStudentsWindow();
        new ControllerConsultStudentsWindow(consultWindow, gradeStage);

        configureAndShowWindow(gradeStage, consultWindow.getView(),
                "Registrar Nota Final",
                STUDENT_CONSULT_WIDTH, STUDENT_CONSULT_HEIGHT);
    }

    private void configureAndShowWindow(Stage stage, Parent root, String title, int width, int height) {
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(title);
        stage.show();
    }

    private void showNotImplementedMessage() {
        System.out.println("Funcionalidad en desarrollo");
    }

    private void handleLogout(ActionEvent event) {
        stage.close();
        onLogout.run();
    }

    @Override
    public void handle(ActionEvent event) {
    }
}
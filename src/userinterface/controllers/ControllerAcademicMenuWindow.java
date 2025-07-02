package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import logic.enums.AcademicType;
import logic.logicclasses.Academic;
import logic.services.ExceptionManager;
import logic.daos.EvaluationRegistrationDAO;
import userinterface.windows.AcademicMenuWindow;
import userinterface.windows.CreateStudentWindow;
import userinterface.windows.ConsultStudentsWindow;
import userinterface.windows.RegistEvaluationWindow;
import userinterface.windows.ConsultRegisteredEvaluationsWindow;
import userinterface.windows.ConsultGroupStudentEvaluationsWindow;
import userinterface.windows.ConsultGroupStudentReportsWindow;
import java.util.Objects;

public class ControllerAcademicMenuWindow implements EventHandler<ActionEvent> {
    private static final int MAIN_WINDOW_WIDTH = 1024;
    private static final int MAIN_WINDOW_HEIGHT = 768;
    private static final int STUDENT_REGISTER_WIDTH = 700;
    private static final int STUDENT_REGISTER_HEIGHT = 400;
    private static final int STUDENT_CONSULT_WIDTH = 800;
    private static final int STUDENT_CONSULT_HEIGHT = 600;

    private final AcademicMenuWindow view;
    private final Stage stage;
    private final Runnable onLogout;
    private Academic academic;

    public ControllerAcademicMenuWindow(Stage stage, Academic academic, Runnable onLogout) {
        this.stage = Objects.requireNonNull(stage, "Stage no puede ser nulo");
        this.onLogout = Objects.requireNonNull(onLogout, "Callback de logout no puede ser nulo");
        this.view = new AcademicMenuWindow(Objects.requireNonNull(academic, "Academic no puede ser nulo"));
        this.academic = academic;

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
                showErrorDialog("Tipo académico no soportado: " + type);
                disableAllButtons();
        }
    }

    private void disableAllButtons() {
        view.getRegisterStudentButton().setDisable(true);
        view.getConsultStudentsButton().setDisable(true);
        view.getRegisterFinalGradeButton().setDisable(true);
        view.getConsultPresentationEvaluationsButton().setDisable(true);
        view.getConsultGroupStudentReportsButton().setDisable(true);
        view.getRegisterPartialEvaluationButton().setDisable(true);
        view.getConsultPartialEvaluationsButton().setDisable(true);
    }

    private void configureEEButtons() {
        view.getRegisterStudentButton().setOnAction(e -> showRegisterStudentWindow());
        view.getConsultStudentsButton().setOnAction(e -> showConsultStudentsWindow());
        view.getRegisterFinalGradeButton().setOnAction(e -> showRegisterFinalGradeWindow());
        view.getConsultPresentationEvaluationsButton().setOnAction(e -> showConsultGroupStudentEvaluationsWindow());
        view.getConsultGroupStudentReportsButton().setOnAction(e -> showConsultGroupStudentReportsWindow());
    }

    private void configureEvaluatorButtons() {
        view.getRegisterPartialEvaluationButton().setOnAction(e -> showRegistEvaluationWindow());
        view.getConsultPartialEvaluationsButton().setOnAction(e -> showConsultRegisteredEvaluationsWindow());

        try {
            EvaluationRegistrationDAO dao = new EvaluationRegistrationDAO();
            boolean enabled = dao.isRegistrationEnabled();
            view.getRegisterPartialEvaluationButton().setDisable(!enabled);
        } catch (Exception e) {
            view.getRegisterPartialEvaluationButton().setDisable(true);
        }
    }

    private void showRegisterStudentWindow() {
        try {
            Stage registerStage = new Stage();
            CreateStudentWindow registerWindow = new CreateStudentWindow();
            new ControllerCreateStudentWindow(registerWindow, academic);

            configureAndShowWindow(registerStage, registerWindow.getView(),
                    "Registrar Estudiante",
                    STUDENT_REGISTER_WIDTH, STUDENT_REGISTER_HEIGHT);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    private void showConsultStudentsWindow() {
        try {
            Stage consultStage = new Stage();
            ConsultStudentsWindow consultWindow = new ConsultStudentsWindow();
            new ControllerConsultStudentsWindow(consultWindow, consultStage, academic);

            configureAndShowWindow(consultStage, consultWindow.getView(),
                    "Consultar Estudiantes",
                    STUDENT_CONSULT_WIDTH, STUDENT_CONSULT_HEIGHT);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    private void showRegisterFinalGradeWindow() {
        try {
            Stage gradeStage = new Stage();
            ConsultStudentsWindow consultWindow = new ConsultStudentsWindow();
            new ControllerConsultStudentsWindow(consultWindow, gradeStage, academic);

            configureAndShowWindow(gradeStage, consultWindow.getView(),
                    "Registrar Nota Final",
                    STUDENT_CONSULT_WIDTH, STUDENT_CONSULT_HEIGHT);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    private void configureAndShowWindow(Stage stage, Parent root, String title, int width, int height) {
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(title);
        stage.show();
    }

    private void handleLogout(ActionEvent event) {
        stage.close();
        onLogout.run();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Ha ocurrido un error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showRegistEvaluationWindow() {
        try {
            Stage evalStage = new Stage();
            RegistEvaluationWindow evalWindow = new RegistEvaluationWindow();
            new ControllerRegistEvaluationWindow(evalWindow, evalStage, academic);

            evalStage.setScene(new Scene(evalWindow.getView(), 600, 600));
            evalStage.setTitle("Registrar Evaluación Parcial");
            evalStage.show();
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    private void showConsultRegisteredEvaluationsWindow() {
        try {
            Stage consultStage = new Stage();
            ConsultRegisteredEvaluationsWindow consultWindow = new ConsultRegisteredEvaluationsWindow();
            new ControllerConsultRegisteredEvaluationsWindow(consultWindow, consultStage, academic);
            consultStage.setScene(new javafx.scene.Scene(consultWindow.getView(), 900, 400));
            consultStage.setTitle("Evaluaciones Registradas");
            consultStage.show();
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    private void showConsultGroupStudentEvaluationsWindow() {
        try {
            Stage consultStage = new Stage();
            ConsultGroupStudentEvaluationsWindow consultWindow = new ConsultGroupStudentEvaluationsWindow();
            new ControllerConsultGroupStudentEvaluationsWindow(consultWindow, consultStage, academic);
            consultStage.setScene(new Scene(consultWindow.getView(), 900, 500));
            consultStage.setTitle("Evaluaciones de Estudiantes del Grupo");
            consultStage.show();
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    private void showConsultGroupStudentReportsWindow() {
        try {
            Stage consultStage = new Stage();
            ConsultGroupStudentReportsWindow consultWindow = new ConsultGroupStudentReportsWindow();
            new ControllerConsultGroupStudentReportsWindow(consultWindow, consultStage, academic);
            consultStage.setScene(new Scene(consultWindow.getView(), 900, 500));
            consultStage.setTitle("Reportes de Estudiantes del Grupo");
            consultStage.show();
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showErrorDialog(message);
        }
    }

    @Override
    public void handle(ActionEvent event) {
    }
}
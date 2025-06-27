package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.daos.GroupDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Academic;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.windows.ConsultGroupStudentEvaluationsWindow;
import userinterface.windows.ConsultStudentEvaluationsWindow;
import userinterface.windows.ConsultSelfEvaluationWindow;

import java.util.List;

public class ControllerConsultGroupStudentEvaluationsWindow {
    private final ConsultGroupStudentEvaluationsWindow view;
    private final Stage stage;
    private final Academic academic;
    private ObservableList<Student> students = FXCollections.observableArrayList();

    public ControllerConsultGroupStudentEvaluationsWindow(ConsultGroupStudentEvaluationsWindow view, Stage stage, Academic academic) {
        this.view = view;
        this.stage = stage;
        this.academic = academic;
        loadStudents();
        setupHandlers();
    }

    private void loadStudents() {
        try {
            GroupDAO groupDAO = new GroupDAO();
            StudentDAO studentDAO = new StudentDAO();
            int groupId = groupDAO.getGroupByAcademicId(academic.getIdUser()).getNrc();
            List<Student> studentList = studentDAO.getActiveStudentsByGroup(groupId);
            students.setAll(studentList);
            view.setStudentData(students);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            view.showAlert(Alert.AlertType.ERROR, "Error de carga",
                "No se pudieron cargar los estudiantes: " + message);
        }
    }

    private void setupHandlers() {
        view.setOnShowEvaluations(event -> {
            Object source = event.getSource();
            if (source instanceof Student) {
                Student student = (Student) source;
                ConsultStudentEvaluationsWindow evalView = new ConsultStudentEvaluationsWindow();
                Stage evalStage = new Stage();
                new ControllerConsultStudentEvaluationsWindow(evalView, evalStage, student);
                evalStage.setScene(new Scene(evalView.getView(), 800, 400));
                evalStage.setTitle("Evaluaciones de " + student.getFullName());
                evalStage.show();
            }
        });

        view.setOnShowSelfEvaluation(event -> {
            Object source = event.getSource();
            if (source instanceof Student) {
                Student student = (Student) source;
                ConsultSelfEvaluationWindow selfEvalView = new ConsultSelfEvaluationWindow();
                Stage selfEvalStage = new Stage();
                new ControllerConsultSelfEvaluationWindow(selfEvalView, selfEvalStage, student);
                selfEvalStage.setScene(new Scene(selfEvalView.getView(), 600, 300));
                selfEvalStage.setTitle("Autoevaluación de " + student.getFullName());
                selfEvalStage.show();
            }
        });

        view.getBackButton().setOnAction(e -> stage.close());
    }
}


package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.daos.StudentDAO;
import logic.logicclasses.Student;
import userinterface.utilities.Validators;
import userinterface.windows.AssignGradeWindow;
import userinterface.windows.ConsultStudentsWindow;
import userinterface.windows.UpdateStudentWindow;
import java.sql.SQLException;

public class ControllerConsultStudentsWindow {
    private final ConsultStudentsWindow view;
    private final StudentDAO studentDAO;
    private final Stage currentStage;
    private final Validators validators;
    private ObservableList<Student> allStudents;
    private boolean hasSearchResults = false;

    public ControllerConsultStudentsWindow(ConsultStudentsWindow view, Stage stage) {
        this.view = view;
        this.studentDAO = new StudentDAO();
        this.currentStage = stage;
        this.validators = new Validators();
        this.allStudents = FXCollections.observableArrayList();

        TableColumn<Student, Void> manageCol = view.createManageButtonColumn(this::handleManageStudent);
        TableColumn<Student, Void> assignGradeCol = view.createAssignGradeButtonColumn(this::handleAssignGrade);
        view.getStudentTable().getColumns().addAll(manageCol, assignGradeCol);

        setupEventHandlers();
        loadStudentData();
    }

    private void setupEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchStudentByEnrollment());
        view.getSearchField().setOnAction(e -> searchStudentByEnrollment());
        view.getClearButton().setOnAction(e -> clearSearch());
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadStudentData() {
        try {
            allStudents.setAll(studentDAO.getSudentsByStatus('A'));
            view.setStudentData(allStudents);
            view.getSearchField().clear();
            hasSearchResults = false;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los estudiantes: " + e.getMessage());
        }
    }

    private void searchStudentByEnrollment() {
        String enrollment = view.getSearchField().getText().trim();

        if (!validators.validateEnrollment(enrollment)) {
            showAlert(Alert.AlertType.WARNING, "Formato incorrecto",
                    "La matrícula debe comenzar con S seguida de 8 dígitos (Ej: S12345678)");
            return;
        }

        try {
            Student student = studentDAO.getStudentByEnrollment(enrollment);

            if (student.getIdUser() != -1) {
                ObservableList<Student> searchResult = FXCollections.observableArrayList();
                searchResult.add(student);
                view.setStudentData(searchResult);
                hasSearchResults = true;
            } else {
                view.setStudentData(FXCollections.observableArrayList()); // Limpiar tabla
                showAlert(Alert.AlertType.INFORMATION, "Búsqueda",
                        "No se encontró ningún estudiante con la matrícula: " + enrollment);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de búsqueda",
                    "Ocurrió un error al buscar el estudiante: " + e.getMessage());
        }
    }

    private void clearSearch() {
        view.getSearchField().clear();
        if (hasSearchResults) {
            loadStudentData();
        } else {
            view.setStudentData(allStudents);
        }
    }

    private void handleManageStudent(ActionEvent event) {
        Student student = (Student) event.getSource();
        openUpdateStudentWindow(student);
    }

    private void handleAssignGrade(ActionEvent event) {
        Student student = (Student) event.getSource();
        openAssignGradeWindow(student);
    }

    private void openUpdateStudentWindow(Student student) {
        try {
            UpdateStudentWindow updateWindow = new UpdateStudentWindow();
            Stage updateStage = new Stage();

            new ControllerUpdateStudentWindow(
                    updateWindow,
                    student,
                    updateStage,
                    this::loadStudentData
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(updateWindow.getView(), 600, 400);
            updateStage.setScene(scene);
            updateStage.setTitle("Actualizar Estudiante");
            updateStage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir la ventana de actualización: " + e.getMessage());
        }
    }

    private void openAssignGradeWindow(Student student) {
        try {
            AssignGradeWindow gradeWindow = new AssignGradeWindow(student);
            Stage gradeStage = new Stage();

            new ControllerAssignGradeWindow(
                    gradeWindow,
                    student,
                    this::loadStudentData
            );

            gradeStage.setScene(new javafx.scene.Scene(gradeWindow.getView()));
            gradeStage.setTitle("Asignar Calificación");
            gradeStage.initModality(Modality.APPLICATION_MODAL);
            gradeStage.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir la ventana de calificación: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
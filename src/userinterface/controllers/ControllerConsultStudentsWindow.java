package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.StudentDAO;
import logic.logicclasses.Student;
import userinterface.windows.ConsultStudentsWindow;
import userinterface.windows.UpdateStudentWindow;

import java.sql.SQLException;

public class ControllerConsultStudentsWindow {
    private final ConsultStudentsWindow view;
    private final StudentDAO studentDAO;
    private final Stage currentStage;

    public ControllerConsultStudentsWindow(ConsultStudentsWindow view, Stage stage) {
        this.view = view;
        this.studentDAO = new StudentDAO();
        this.currentStage = stage;

        // Configura la columna "Gestionar" con el manejador
        TableColumn<Student, Void> manageCol = view.createManageButtonColumn(event -> {
            Student student = (Student) event.getSource();
            openUpdateStudentWindow(student);
        });
        view.getStudentTable().getColumns().add(manageCol);

        setupEventHandlers();
        loadStudentData();
    }

    private void setupEventHandlers() {
        view.getRefreshButton().setOnAction(e -> loadStudentData());
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadStudentData() {
        try {
            ObservableList<Student> students =
                    FXCollections.observableArrayList(studentDAO.getAllStudents());
            view.setStudentData(students);
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los estudiantes: " + e.getMessage());
        }
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
            showAlert("Error", "No se pudo abrir la ventana de actualización: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
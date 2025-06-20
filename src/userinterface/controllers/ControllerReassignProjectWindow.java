package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.*;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import logic.services.PDFAssignmentGenerator;
import userinterface.windows.ReassignProjectWindow;

import java.sql.SQLException;

public class ControllerReassignProjectWindow implements EventHandler<ActionEvent> {
    private final ReassignProjectWindow view;
    private final ProjectStudentDAO projectStudentDAO;
    private final StudentDAO studentDAO;
    private final ProjectDAO projectDAO;
    private final Stage stage;

    public ControllerReassignProjectWindow(Stage parentStage) {
        this.view = new ReassignProjectWindow();
        this.projectStudentDAO = new ProjectStudentDAO();
        this.studentDAO = new StudentDAO();
        this.projectDAO = new ProjectDAO();

        this.stage = new Stage();
        stage.initOwner(parentStage);
        stage.setScene(new Scene(view.getView(), 600, 500));
        stage.setTitle("Reasignar Proyecto");

        setupEventHandlers();
        loadData();

        view.getStudentsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, student) -> {
            if (student != null) {
                updateCurrentProjectDisplay(student.getIdUser());
            }
        });
    }

    private void setupEventHandlers() {
        view.getReassignButton().setOnAction(this);
        view.getCancelButton().setOnAction(this);
    }

    private void loadData() {
        try {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    studentDAO.getAllStudents()
            );
            view.setStudentsList(students);

            ObservableList<Project> projects = FXCollections.observableArrayList(
                    projectDAO.getAllProyects()
            );
            view.setProjectsList(projects);

        } catch (SQLException e) {
            view.showMessage("Error al cargar datos: " + e.getMessage(), true);
        }
    }

    private void updateCurrentProjectDisplay(int studentId) {
        try {
            Integer projectId = projectStudentDAO.getProyectByStudent(studentId);
            if (projectId != null) {
                Project project = projectDAO.getProyectById(projectId);
                view.getCurrentProjectLabel().setText(
                        "Proyecto actual: " + (project != null ? project.getTitle() : "Desconocido")
                );
            } else {
                view.getCurrentProjectLabel().setText("Proyecto actual: Ninguno");
            }
        } catch (SQLException e) {
            view.showMessage("Error al obtener proyecto actual: " + e.getMessage(), true);
        }
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getCancelButton()) {
            stage.close();
        } else if (event.getSource() == view.getReassignButton()) {
            handleReassign();
        }
    }

    private void handleReassign() {
        Student selectedStudent = view.getStudentsTable().getSelectionModel().getSelectedItem();
        Project selectedProject = view.getProjectsComboBox().getSelectionModel().getSelectedItem();

        if (selectedStudent == null || selectedProject == null) {
            view.showMessage("Debe seleccionar un estudiante y un proyecto", true);
            return;
        }

        try {
            Integer currentProjectId = projectStudentDAO.getProyectByStudent(selectedStudent.getIdUser());

            AssignmentDocumentDAO assignmentDocumentDAO = new AssignmentDocumentDAO();
            assignmentDocumentDAO.deleteAssignmentDocument(selectedStudent.getIdUser());

            if (currentProjectId != null) {
                projectStudentDAO.removeStudentFromProyect(currentProjectId, selectedStudent.getIdUser());
            }

            boolean success = projectStudentDAO.assignStudentToProject(
                    selectedProject.getIdProyect(),
                    selectedStudent.getIdUser()
            );

            if (success) {
                PDFAssignmentGenerator pdfAssignmentGenerator = new PDFAssignmentGenerator();
                byte[] pdfContent = pdfAssignmentGenerator.generateAssignmentPDF(
                        selectedStudent,
                        selectedProject
                );
                assignmentDocumentDAO.saveAssignmentDocument(
                        selectedProject.getIdProyect(),
                        selectedStudent.getIdUser(),
                        pdfContent
                );

                view.showMessage("Estudiante reasignado y documento actualizado correctamente", false);
                updateCurrentProjectDisplay(selectedStudent.getIdUser());
            } else {
                view.showMessage("No se pudo reasignar el estudiante", true);
            }

        } catch (Exception e) {
            view.showMessage("Error al reasignar: " + e.getMessage(), true);
        }
    }

    public void show() {
        stage.show();
    }
}
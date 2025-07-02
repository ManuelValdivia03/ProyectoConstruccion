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
import logic.services.ExceptionManager;
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
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar datos: " + message, true);
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
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al obtener proyecto actual: " + message, true);
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

            deleteAssignmentDocumentForStudent(selectedStudent.getIdUser());

            if (currentProjectId != null) {
                reassignStudentToProject(currentProjectId, selectedProject.getIdProyect(), selectedStudent.getIdUser());
            } else {
                assignStudentToProject(selectedProject.getIdProyect(), selectedStudent.getIdUser());
            }

            generateAndSaveAssignmentPDF(selectedStudent, selectedProject);

            view.showMessage("Estudiante reasignado y documento actualizado correctamente", false);
            updateCurrentProjectDisplay(selectedStudent.getIdUser());

        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al reasignar: " + message, true);
        }
    }

    private void deleteAssignmentDocumentForStudent(int studentId) throws SQLException {
        AssignmentDocumentDAO assignmentDocumentDAO = new AssignmentDocumentDAO();
        assignmentDocumentDAO.deleteAssignmentDocument(studentId);
    }

    private void reassignStudentToProject(int currentProjectId, int newProjectId, int studentId) throws SQLException {
        projectStudentDAO.removeStudentFromProyect(currentProjectId, studentId);
        assignStudentToProject(newProjectId, studentId);
    }

    private void assignStudentToProject(int projectId, int studentId) throws SQLException {
        boolean success = projectStudentDAO.assignStudentToProject(projectId, studentId);
        if (!success) {
            throw new SQLException("No se pudo reasignar el estudiante");
        }
    }

    private void generateAndSaveAssignmentPDF(Student student, Project project) throws Exception {
        byte[] pdfContent = generateAssignmentPDF(student, project);
        saveAssignmentDocument(project, student, pdfContent);
    }

    private byte[] generateAssignmentPDF(Student student, Project project) throws Exception {
        PDFAssignmentGenerator pdfAssignmentGenerator = new PDFAssignmentGenerator();
        return pdfAssignmentGenerator.generateAssignmentPDF(student, project);
    }

    private void saveAssignmentDocument(Project project, Student student, byte[] pdfContent) throws SQLException {
        AssignmentDocumentDAO assignmentDocumentDAO = new AssignmentDocumentDAO();
        assignmentDocumentDAO.saveAssignmentDocument(
                project.getIdProyect(),
                student.getIdUser(),
                pdfContent
        );
    }

    public void show() {
        stage.show();
    }
}
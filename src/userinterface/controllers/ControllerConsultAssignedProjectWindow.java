package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.daos.*;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import logic.services.PDFAssignmentGenerator;
import userinterface.windows.ConsultAssignedProjectWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ControllerConsultAssignedProjectWindow implements EventHandler<ActionEvent> {
    private final ConsultAssignedProjectWindow view;
    private final ProjectDAO projectDAO;
    private final ProjectStudentDAO projectStudentDAO;
    private final AssignmentDocumentDAO assignmentDocumentDAO;
    private final StudentDAO studentDAO;
    private final int studentId;
    private final Stage stage;
    private Project assignedProject;

    public ControllerConsultAssignedProjectWindow(Stage parentStage, int studentId) {
        this.view = new ConsultAssignedProjectWindow();
        this.projectDAO = new ProjectDAO();
        this.projectStudentDAO = new ProjectStudentDAO();
        this.assignmentDocumentDAO = new AssignmentDocumentDAO();
        this.studentDAO = new StudentDAO();
        this.studentId = studentId;

        this.stage = new Stage();
        stage.initOwner(parentStage);
        stage.setScene(new Scene(view.getView(), 600, 500));
        stage.setTitle("Proyecto Asignado");

        setupEventHandlers();
        loadAssignedProject();
    }

    private void setupEventHandlers() {
        view.getDownloadButton().setOnAction(this);
        view.getCloseButton().setOnAction(this);
    }

    private void loadAssignedProject() {
        try {
            Integer projectId = projectStudentDAO.getProyectByStudent(studentId);

            if (projectId != null) {
                assignedProject = projectDAO.getProyectById(projectId);

                if (assignedProject == null
                        || assignedProject.getIdProyect() == -1
                        || assignedProject.getDateStart() == null
                        || assignedProject.getDateEnd() == null) {
                    view.showMessage("No tienes ningún proyecto asignado", true);
                    view.getDownloadButton().setDisable(true);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate startDate = assignedProject.getDateStart().toLocalDateTime().toLocalDate();
                    LocalDate endDate = assignedProject.getDateEnd().toLocalDateTime().toLocalDate();

                    view.setProjectData(
                            assignedProject.getTitle(),
                            assignedProject.getDescription(),
                            startDate.format(formatter),
                            endDate.format(formatter)
                    );
                }
            } else {
                view.showMessage("No tienes ningún proyecto asignado", true);
                view.getDownloadButton().setDisable(true);
            }
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar el proyecto asignado: " + message, true);
        }
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getCloseButton()) {
            stage.close();
        } else if (event.getSource() == view.getDownloadButton()) {
            handleDownloadAssignment();
        }
    }

    private void handleDownloadAssignment() {
        try {
            byte[] pdfContent = assignmentDocumentDAO.getAssignmentDocument(
                    studentId
            );

            if (pdfContent != null && pdfContent.length > 0) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Guardar Oficio de Asignación");
                fileChooser.setInitialFileName(
                        "Oficio_Asignacion_" + studentDAO.getStudentById(studentId).getEnrollment() + ".pdf"
                );
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                );

                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(pdfContent);
                        view.showMessage("Oficio descargado correctamente", false);
                    } catch (IOException e) {
                        view.showMessage("Error al guardar el archivo: " + e.getMessage(), true);
                    }
                }
            } else {
                byte[] generatedPdf = generateAssignmentPDFContent();
                if (generatedPdf != null && generatedPdf.length > 0) {
                    saveAssignmentToFile(generatedPdf);
                } else {
                    view.showMessage("No se pudo generar el oficio de asignación.", true);
                }
            }
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al obtener el oficio: " + message, true);
        }
    }

    private byte[] generateAssignmentPDFContent() {
        try {
            PDFAssignmentGenerator pdfGenerator = new PDFAssignmentGenerator();
            Student student = studentDAO.getStudentById(studentId);
            if (student == null) {
                student = new Student();
            }

            byte[] pdfContent = pdfGenerator.generateAssignmentPDF(student, assignedProject);

            assignmentDocumentDAO.saveAssignmentDocument(
                    assignedProject.getIdProyect(),
                    studentId,
                    pdfContent
            );
            return pdfContent != null ? pdfContent : new byte[0];
        } catch (SQLException | IOException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al generar el oficio: " + message, true);
            return new byte[0];
        }
    }

    private void saveAssignmentToFile(byte[] pdfContent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Oficio de Asignación");
        try {
            Student student = studentDAO.getStudentById(studentId);
            if (student == null) {
                student = new Student();
            }
            fileChooser.setInitialFileName(
                    "Oficio_Asignacion_" + student.getEnrollment() + ".pdf"
            );
        } catch (SQLException e) {
            fileChooser.setInitialFileName("Oficio_Asignacion.pdf");
        }
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfContent);
                view.showMessage("Oficio generado y descargado correctamente", false);
            } catch (IOException e) {
                view.showMessage("Error al guardar el archivo: " + e.getMessage(), true);
            }
        }
    }

    public void show() {
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }
}
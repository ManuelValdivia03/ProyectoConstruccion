package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.*;
import logic.logicclasses.Project;
import logic.logicclasses.ProjectRequest;

import logic.services.PDFAssignmentGenerator;
import logic.services.ExceptionManager;
import userinterface.windows.CoordinatorProjectsWindow;
import userinterface.windows.ProjectRequestsWindow;

import java.io.IOException;
import java.sql.SQLException;

public class ControllerCoordinatorProjectsWindow implements EventHandler<ActionEvent> {
    private final CoordinatorProjectsWindow view;
    private final ProjectDAO projectDAO;
    private final ProjectRequestDAO requestDAO;
    private final ProjectStudentDAO projectStudentDAO;
    private final AssignmentDocumentDAO assignmentDocumentDAO;
    private final ObservableList<Project> projectsList;
    private ProjectRequestsWindow requestsWindow;

    public ControllerCoordinatorProjectsWindow(CoordinatorProjectsWindow window) {
        this.view = window;
        this.projectDAO = new ProjectDAO();
        this.requestDAO = new ProjectRequestDAO();
        this.projectStudentDAO = new ProjectStudentDAO();
        this.assignmentDocumentDAO = new AssignmentDocumentDAO();
        this.projectsList = FXCollections.observableArrayList();

        view.getProjectsTable().getProperties().put("controller", this);
        setupEventHandlers();
        loadProjects();
    }

    private void setupEventHandlers() {
        view.getViewRequestsButton().setOnAction(this);
        view.getRefreshButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getRefreshButton()) {
            loadProjects();
        }
    }

    public void showRequestsForProject(Project project) {
        try {
            this.requestsWindow = new ProjectRequestsWindow();
            ObservableList<ProjectRequest> requests = FXCollections.observableArrayList(
                    requestDAO.getRequestsByProject(project.getIdProyect())
            );

            requestsWindow.setRequestsList(requests);
            requestsWindow.getApproveButton().setOnAction(e -> handleApproveRequests(project, requests));
            requestsWindow.getRejectButton().setOnAction(e -> handleRejectRequests(project, requests));

            Stage stage = new Stage();
            stage.setScene(new Scene(requestsWindow.getView(), 500, 400));
            stage.setTitle("Solicitudes - " + project.getTitle());
            stage.show();
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar solicitudes: " + message, true);
        }
    }

    private void handleApproveRequests(Project project, ObservableList<ProjectRequest> requests) {
        ObservableList<ProjectRequest> selectedRequests = requestsWindow.getRequestsTable().getSelectionModel().getSelectedItems();
        try {
            for (ProjectRequest request : selectedRequests) {
                if (request.isPending() && project.getCapacity() > 0) {
                    approveRequestAndGenerateDocument(request, project);
                } else if (project.getCapacity() <= 0) {
                    rejectSingleRequest(request);
                }
            }
            loadProjects();
            view.showMessage("Solicitudes aprobadas y documentos generados correctamente", false);
        } catch (SQLException | IOException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al procesar solicitudes: " + message, true);
        }
    }

    private void approveRequestAndGenerateDocument(ProjectRequest request, Project project) throws SQLException, IOException {
        requestDAO.approveRequest(request.getRequestId(), projectStudentDAO);

        StudentDAO studentDAO = new StudentDAO();
        byte[] pdfContent = generateAssignmentPDFContent(studentDAO.getStudentById(request.getStudentId()), project);

        saveAssignmentDocument(project.getIdProyect(), request.getStudentId(), pdfContent);
    }

    private byte[] generateAssignmentPDFContent(logic.logicclasses.Student student, Project project) throws IOException {
        PDFAssignmentGenerator pdfAssignmentGenerator = new PDFAssignmentGenerator();
        try {
            return pdfAssignmentGenerator.generateAssignmentPDF(student, project);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveAssignmentDocument(int projectId, int studentId, byte[] pdfContent) throws SQLException {
        assignmentDocumentDAO.saveAssignmentDocument(projectId, studentId, pdfContent);
    }

    private void rejectSingleRequest(ProjectRequest request) throws SQLException {
        requestDAO.rejectRequest(request.getRequestId());
    }

    private void handleRejectRequests(Project project, ObservableList<ProjectRequest> requests) {
        ObservableList<ProjectRequest> selectedRequests = requestsWindow.getRequestsTable().getSelectionModel().getSelectedItems();
        try {
            for (ProjectRequest request : selectedRequests) {
                if (request.isPending()) {
                    rejectSingleRequest(request);
                }
            }
            view.showMessage("Solicitudes rechazadas correctamente", false);
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al rechazar solicitudes: " + message, true);
        }
    }

    private void loadProjects() {
        try {
            projectsList.setAll(projectDAO.getAllProyects());
            view.setProjectsList(projectsList);
            view.showMessage("", false);
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar proyectos: " + message, true);
        }
    }
}
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
import logic.logicclasses.Student;
import logic.services.PDFAssignmentGenerator;
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
            view.showMessage("Error al cargar solicitudes: " + e.getMessage(), true);
        }
    }

    private void handleApproveRequests(Project project, ObservableList<ProjectRequest> requests) {
        ObservableList<ProjectRequest> selectedRequests = requestsWindow.getRequestsTable().getSelectionModel().getSelectedItems();
        try {
            for (ProjectRequest request : selectedRequests) {
                if (request.isPending() && project.getCapacity() > 0) {
                    requestDAO.approveRequest(request.getRequestId(), projectStudentDAO);

                    StudentDAO studentDAO = new StudentDAO();
                    PDFAssignmentGenerator pdfAssignmentGenerator = new PDFAssignmentGenerator();

                    byte[] pdfContent = pdfAssignmentGenerator.generateAssignmentPDF(
                            studentDAO.getStudentById(request.getStudentId()),
                            project
                    );

                    assignmentDocumentDAO.saveAssignmentDocument(
                            project.getIdProyect(),
                            request.getStudentId(),
                            pdfContent
                    );

                } else if (project.getCapacity() <= 0) {
                    requestDAO.rejectRequest(request.getRequestId());
                }
            }
            loadProjects();
            view.showMessage("Solicitudes aprobadas y documentos generados correctamente", false);
        } catch (SQLException | IOException e) {
            view.showMessage("Error al procesar solicitudes: " + e.getMessage(), true);
        }
    }

    private void handleRejectRequests(Project project, ObservableList<ProjectRequest> requests) {
        ObservableList<ProjectRequest> selectedRequests = requestsWindow.getRequestsTable().getSelectionModel().getSelectedItems();
        try {
            for (ProjectRequest request : selectedRequests) {
                if (request.isPending()) {
                    requestDAO.rejectRequest(request.getRequestId());
                }
            }
            view.showMessage("Solicitudes rechazadas correctamente", false);
        } catch (SQLException e) {
            view.showMessage("Error al rechazar solicitudes: " + e.getMessage(), true);
        }
    }

    private void loadProjects() {
        try {
            projectsList.setAll(projectDAO.getAllProyects());
            view.setProjectsList(projectsList);
            view.showMessage("", false);
        } catch (SQLException e) {
            view.showMessage("Error al cargar proyectos: " + e.getMessage(), true);
        }
    }
}
package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.ProjectDAO;
import logic.daos.ProjectRequestDAO;
import logic.daos.ProjectStudentDAO;
import logic.logicclasses.Project;
import logic.logicclasses.ProjectRequest;
import userinterface.windows.CoordinatorProjectsWindow;
import userinterface.windows.ProjectRequestsWindow;

import java.sql.SQLException;

public class ControllerCoordinatorProjectsWindow implements EventHandler<ActionEvent> {
    private final CoordinatorProjectsWindow view;
    private final ProjectDAO projectDAO;
    private final ProjectRequestDAO requestDAO;
    private final ObservableList<Project> projectsList;

    public ControllerCoordinatorProjectsWindow(CoordinatorProjectsWindow window) {
        this.view = window;
        this.projectDAO = new ProjectDAO();
        this.requestDAO = new ProjectRequestDAO();
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
            ProjectRequestsWindow requestsWindow = new ProjectRequestsWindow();
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
        ProjectStudentDAO projectStudentDAO = new ProjectStudentDAO();
        try {
            for (ProjectRequest request : requests) {
                if (request.isPending() && project.getCapacity() > 0) {
                    requestDAO.approveRequest(request.getRequestId(), projectStudentDAO);
                    projectDAO.incrementStudentCount(project.getIdProyect());
                } else if (project.getCapacity() <= 0) {
                    requestDAO.rejectRequest(request.getRequestId());
                }
            }
            loadProjects();
            view.showMessage("Solicitudes procesadas correctamente", false);
        } catch (SQLException e) {
            view.showMessage("Error al procesar solicitudes: " + e.getMessage(), true);
        }
    }

    private void handleRejectRequests(Project project, ObservableList<ProjectRequest> requests) {
        try {
            for (ProjectRequest request : requests) {
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
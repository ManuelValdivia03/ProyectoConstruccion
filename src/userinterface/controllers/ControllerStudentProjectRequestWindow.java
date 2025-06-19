package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.ProjectDAO;
import logic.daos.ProjectRequestDAO;
import logic.enums.RequestStatus;
import logic.logicclasses.Project;
import logic.logicclasses.ProjectRequest;
import logic.logicclasses.User;
import userinterface.windows.StudentProjectRequestWindow;

import java.sql.SQLException;
import java.sql.Timestamp;

public class ControllerStudentProjectRequestWindow implements EventHandler<ActionEvent> {
    private final StudentProjectRequestWindow view;
    private final ProjectDAO projectDAO;
    private final ProjectRequestDAO requestDAO;
    private final User currentStudent;
    private final ObservableList<Project> projectsList;

    public ControllerStudentProjectRequestWindow(StudentProjectRequestWindow window, User student) {
        this.view = window;
        this.projectDAO = new ProjectDAO();
        this.requestDAO = new ProjectRequestDAO();
        this.currentStudent = student;
        this.projectsList = FXCollections.observableArrayList();

        view.getProjectsTable().getProperties().put("controller", this);

        view.getSearchButton().setOnAction(this);
        view.getBackButton().setOnAction(this);

        loadProjects();
    }

    private void loadProjects() {
        try {
            projectsList.setAll(projectDAO.getAvailableProjects());
            view.setProjectsList(projectsList);
            view.getResultLabel().setText("");
        } catch (SQLException e) {
            view.showError("Error al cargar proyectos: " + e.getMessage());
        }
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getSearchButton()) {
            handleSearch();
        } else if (event.getSource() == view.getBackButton()) {
            view.getView().getScene().getWindow().hide();
        }
    }

    public void handleRequest(Project project) {
        try {
            if (requestDAO.hasExistingRequest(project.getIdProyect(), currentStudent.getIdUser())) {
                view.showError("Ya has enviado una solicitud para este proyecto anteriormente");
                return;
            }
            ProjectRequest request = new ProjectRequest(0, project.getIdProyect(),
                    currentStudent.getIdUser(), new Timestamp(System.currentTimeMillis()),
                    RequestStatus.PENDIENTE, project.getTitle(), currentStudent.getFullName());
            boolean success = requestDAO.createRequest(request);
            if (success) {
                view.showSuccess("Solicitud enviada correctamente");
                loadProjects();
            } else {
                view.showError("No se pudo enviar la solicitud");
            }
        } catch (SQLException e) {
            view.showError("Error inesperado al enviar la solicitud. Por favor, inténtelo más tarde.");
        }
    }

    private void handleSearch() {
        String searchTerm = view.getSearchField().getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadProjects();
            return;
        }

        ObservableList<Project> filtered = FXCollections.observableArrayList();
        for (Project p : projectsList) {
            if (p.getTitle().toLowerCase().contains(searchTerm) ||
                    p.getDescription().toLowerCase().contains(searchTerm)) {
                filtered.add(p);
            }
        }
        view.setProjectsList(filtered);
    }
}

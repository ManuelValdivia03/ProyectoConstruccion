package userinterface.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.ProjectDAO;
import logic.logicclasses.Project;
import userinterface.windows.ConsultProyectsWindow;
import userinterface.windows.UpdateProyectWindow;

import java.sql.SQLException;
import java.util.Objects;

public class ControllerConsultProyectsWindow implements EventHandler<ActionEvent> {
    private final ConsultProyectsWindow view;
    private final ProjectDAO projectDAO;
    private ObservableList<Project> projectsList;

    public ControllerConsultProyectsWindow(ConsultProyectsWindow consultProyectsWindow) {
        this.view = Objects.requireNonNull(consultProyectsWindow, "La vista no puede ser nula");
        this.projectDAO = new ProjectDAO();
        this.projectsList = FXCollections.observableArrayList();

        view.getProjectsTable().getProperties().put("controller", this);

        setupEventHandlers();
        loadAllProjects();
    }

    private void setupEventHandlers() {
        view.getSearchButton().setOnAction(this);
        view.getRefreshButton().setOnAction(this);
        view.getBackButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getSearchButton()) {
            handleSearch();
        } else if (event.getSource() == view.getRefreshButton()) {
            loadAllProjects();
        } else if (event.getSource() == view.getBackButton()) {
            handleBack();
        }
    }

    private void loadAllProjects() {
        try {
            projectsList.setAll(projectDAO.getProyectsByStatus('A'));
            view.setProjectsList(projectsList);
            view.getResultLabel().setText("");
        } catch (SQLException e) {
            showError("Error al cargar proyectos: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String searchTerm = view.getSearchField().getText().trim();
        if (searchTerm.isEmpty()) {
            loadAllProjects();
            return;
        }

        try {
            Project foundProject = projectDAO.getProyectByTitle(searchTerm);
            if (foundProject != null) {
                projectsList.setAll(foundProject);
                view.setProjectsList(projectsList);
                view.getResultLabel().setText("");
            } else {
                projectsList.clear();
                view.setProjectsList(projectsList);
                showError("No se encontró ningún proyecto con ese título");
            }
        } catch (SQLException e) {
            showError("Error al buscar proyecto: " + e.getMessage());
        }
    }

    private void handleBack() {
        view.getView().getScene().getWindow().hide();
    }

    public void handleEdit(Project project) {
        if (project == null) {
            showError("Proyecto inválido para editar.");
            return;
        }
        UpdateProyectWindow updateProyectWindow = new UpdateProyectWindow();
        new ControllerUpdateProyectWindow(updateProyectWindow, project);

        Stage stage = new Stage();
        stage.setScene(new Scene(updateProyectWindow.getView()));
        stage.setTitle("Editar proyecto");
        stage.showAndWait();
        loadAllProjects();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            view.getResultLabel().setText(message);
            view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
        });
    }
}

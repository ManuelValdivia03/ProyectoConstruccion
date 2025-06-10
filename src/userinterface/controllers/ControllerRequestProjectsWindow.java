package userinterface.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.ProyectDAO;
import logic.logicclasses.Proyect;
import logic.logicclasses.Student;
import userinterface.windows.RequestProjectsWindow;

import java.sql.SQLException;
import java.util.Objects;

public class ControllerRequestProjectsWindow implements EventHandler<ActionEvent> {
    private final RequestProjectsWindow view;
    private final ProyectDAO proyectDAO;
    private final Student student;
    private ObservableList<Proyect> projectsList;

    public ControllerRequestProjectsWindow(RequestProjectsWindow requestProjectsWindow, Student student) {
        this.view = Objects.requireNonNull(requestProjectsWindow, "La vista no puede ser nula");
        this.proyectDAO = new ProyectDAO();
        this.projectsList = FXCollections.observableArrayList();
        this.student = Objects.requireNonNull(student, "El estudiante no puede ser nulo");

        view.getProjectsTable().getProperties().put("controller", this);

        setupEventHandlers();
        loadUnassignedProjects();
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
            loadUnassignedProjects();
        } else if (event.getSource() == view.getBackButton()) {
            handleBack();
        }
    }

    private void loadUnassignedProjects() {
        try {
            projectsList.setAll(proyectDAO.getUnassignedProyects());
            view.setProjectsList(projectsList);
            view.getResultLabel().setText("");
        } catch (SQLException e) {
            showError("Error al cargar proyectos: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String searchTerm = view.getSearchField().getText().trim();
        if (searchTerm.isEmpty()) {
            loadUnassignedProjects();
            return;
        }

        try {
            Proyect foundProyect = proyectDAO.getUnassignedProyectByTitle(searchTerm);
            if (foundProyect != null) {
                projectsList.setAll(foundProyect);
                view.setProjectsList(projectsList);
                view.getResultLabel().setText("");
            } else {
                projectsList.clear();
                view.setProjectsList(projectsList);
                showError("No se encontró ningún proyecto disponible con ese título");
            }
        } catch (SQLException e) {
            showError("Error al buscar proyecto: " + e.getMessage());
        }
    }

    private void handleBack() {
        view.getView().getScene().getWindow().hide();
    }

    public void handleRequest(Proyect proyect) {
        if (proyect == null) {
            showError("Proyecto inválido para solicitar.");
            return;
        }

        try {
            boolean success = proyectDAO.assignProjectToCurrentUser(proyect.getIdProyect(), student);

            if (success) {
                showSuccess("Proyecto solicitado exitosamente");
                loadUnassignedProjects();
            } else {
                showError("No se pudo solicitar el proyecto");
            }
        } catch (SQLException e) {
            showError("Error al solicitar proyecto: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            view.getResultLabel().setText(message);
            view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            view.getResultLabel().setText(message);
            view.getResultLabel().setStyle("-fx-text-fill: #009900;");
        });
    }
}
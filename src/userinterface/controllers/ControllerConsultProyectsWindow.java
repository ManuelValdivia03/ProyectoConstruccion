package userinterface.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.ProyectDAO;
import logic.logicclasses.Proyect;
import userinterface.windows.ConsultProyectsWindow;
import userinterface.windows.UpdateProyectWindow;

import java.sql.SQLException;
import java.util.Objects;

public class ControllerConsultProyectsWindow implements EventHandler<ActionEvent> {
    private final ConsultProyectsWindow view;
    private final ProyectDAO proyectDAO;
    private ObservableList<Proyect> projectsList;

    public ControllerConsultProyectsWindow(ConsultProyectsWindow consultProyectsWindow) {
        this.view = Objects.requireNonNull(consultProyectsWindow, "La vista no puede ser nula");
        this.proyectDAO = new ProyectDAO();
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
            projectsList.setAll(proyectDAO.getProyectsByStatus('A'));
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
            Proyect foundProyect = proyectDAO.getProyectByTitle(searchTerm);
            if (foundProyect != null) {
                projectsList.setAll(foundProyect);
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

    public void handleEdit(Proyect proyect) {
        if (proyect == null) {
            showError("Proyecto inválido para editar.");
            return;
        }
        UpdateProyectWindow updateProyectWindow = new UpdateProyectWindow();
        new ControllerUpdateProyectWindow(updateProyectWindow, proyect);

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

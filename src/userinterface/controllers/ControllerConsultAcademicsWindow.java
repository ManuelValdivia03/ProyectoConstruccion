package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.daos.AcademicDAO;
import logic.logicclasses.Academic;
import userinterface.windows.ConsultAcademicsWindow;

import java.sql.SQLException;

public class ControllerConsultAcademicsWindow implements EventHandler<ActionEvent> {
    private final ConsultAcademicsWindow view;
    private final AcademicDAO academicDAO;
    private final Stage currentStage;

    public ControllerConsultAcademicsWindow(ConsultAcademicsWindow view, Stage stage) {
        this.view = view;
        this.academicDAO = new AcademicDAO();
        this.currentStage = stage;
        setupEventHandlers();
        loadAcademicData();
    }

    private void setupEventHandlers() {
        view.getRefreshButton().setOnAction(this);
        view.getBackButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getRefreshButton()) {
            loadAcademicData();
        } else if (event.getSource() == view.getBackButton()) {
            currentStage.close();
        }
    }

    private void loadAcademicData() {
        try {
            ObservableList<Academic> academics =
                    FXCollections.observableArrayList(academicDAO.getAllAcademicsFromView());
            view.setAcademicData(academics);
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los académicos: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
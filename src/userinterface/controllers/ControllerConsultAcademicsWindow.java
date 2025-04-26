package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.AcademicDAO;
import logic.daos.AccountDAO;
import logic.logicclasses.Academic;
import userinterface.windows.ConsultAcademicsWindow;
import userinterface.windows.UpdateAcademicWindow;

import java.sql.SQLException;

public class ControllerConsultAcademicsWindow {
    private final ConsultAcademicsWindow view;
    private final AcademicDAO academicDAO;
    private final Stage currentStage;

    public ControllerConsultAcademicsWindow(ConsultAcademicsWindow view, Stage stage) {
        this.view = view;
        this.academicDAO = new AcademicDAO();
        this.currentStage = stage;

        // Configura la columna "Gestionar" con el manejador
        TableColumn<Academic, Void> manageCol = view.createManageButtonColumn(event -> {
            Academic academic = (Academic) event.getSource();
            openUpdateAcademicWindow(academic);
        });
        view.getAcademicTable().getColumns().add(manageCol);

        setupEventHandlers();
        loadAcademicData();
    }

    private void setupEventHandlers() {
        view.getRefreshButton().setOnAction(e -> loadAcademicData());
        view.getBackButton().setOnAction(e -> currentStage.close());
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

    private void openUpdateAcademicWindow(Academic academic) {
        try {
            AccountDAO accountDAO = new AccountDAO();
            String email = accountDAO.getAccountByUserId(academic.getIdUser()).getEmail();

            UpdateAcademicWindow updateWindow = new UpdateAcademicWindow();
            Stage updateStage = new Stage();

            new ControllerUpdateAcademicWindow(
                    updateWindow,
                    academic,
                    email,
                    updateStage,
                    this::loadAcademicData
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(updateWindow.getView(), 600, 400);
            updateStage.setScene(scene);
            updateStage.setTitle("Actualizar Académico");
            updateStage.show();
        } catch (SQLException e) {
            showAlert("Error", "No se pudo abrir la ventana de actualización: " + e.getMessage());
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
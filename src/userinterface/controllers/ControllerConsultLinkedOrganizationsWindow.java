package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.logicclasses.LinkedOrganization;
import userinterface.windows.ConsultLinkedOrganizationsWindow;
import userinterface.windows.UpdateLinkedOrganizationWindow;

import java.sql.SQLException;

public class ControllerConsultLinkedOrganizationsWindow {
    private final ConsultLinkedOrganizationsWindow view;
    private final LinkedOrganizationDAO organizationDAO;
    private final Stage currentStage;
    private ObservableList<LinkedOrganization> allOrganizations;
    private boolean hasSearchResults = false;

    public ControllerConsultLinkedOrganizationsWindow(ConsultLinkedOrganizationsWindow view, Stage stage) {
        this.view = view;
        this.organizationDAO = new LinkedOrganizationDAO();
        this.currentStage = stage;
        this.allOrganizations = FXCollections.observableArrayList();

        TableColumn<LinkedOrganization, Void> manageCol = view.createManageButtonColumn(this::handleManageOrganization);
        view.getOrganizationTable().getColumns().add(manageCol);

        setupEventHandlers();
        loadOrganizationData();
    }

    private void setupEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchOrganizationByName());
        view.getClearButton().setOnAction(e -> clearSearch());
        view.getRefreshButton().setOnAction(e -> loadOrganizationData());
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadOrganizationData() {
        try {
            allOrganizations.setAll(organizationDAO.getAllLinkedOrganizations());
            view.setOrganizationData(allOrganizations);
            view.getSearchField().clear();
            hasSearchResults = false;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar las organizaciones: " + e.getMessage());
        }
    }

    private void searchOrganizationByName() {
        String name = view.getSearchField().getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia",
                    "Por favor ingrese un nombre para buscar");
            return;
        }

        try {
            LinkedOrganization org = organizationDAO.getLinkedOrganizationByTitle(name);
            if (org != null) {
                ObservableList<LinkedOrganization> searchResult = FXCollections.observableArrayList();
                searchResult.add(org);
                view.setOrganizationData(searchResult);
                hasSearchResults = true;
            } else {
                view.setOrganizationData(FXCollections.observableArrayList());
                hasSearchResults = false;
                showAlert(Alert.AlertType.INFORMATION, "Búsqueda",
                        "No se encontró organización con el nombre: " + name);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de búsqueda",
                    "Ocurrió un error al buscar la organización: " + e.getMessage());
        }
    }

    private void clearSearch() {
        view.getSearchField().clear();
        if (hasSearchResults) {
            loadOrganizationData();
        } else {
            view.setOrganizationData(allOrganizations);
        }
    }

    private void handleManageOrganization(ActionEvent event) {
        LinkedOrganization org = (LinkedOrganization) event.getSource();
        openUpdateOrganizationWindow(org);
    }

    private void openUpdateOrganizationWindow(LinkedOrganization org) {
        try {
            UpdateLinkedOrganizationWindow updateWindow = new UpdateLinkedOrganizationWindow();
            Stage updateStage = new Stage();

            new ControllerUpdateLinkedOrganizationWindow(
                    updateWindow,
                    org,
                    updateStage,
                    v -> loadOrganizationData()
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(updateWindow.getView(), 600, 400);
            updateStage.setScene(scene);
            updateStage.setTitle("Actualizar Organización");
            updateStage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir la ventana de actualización: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
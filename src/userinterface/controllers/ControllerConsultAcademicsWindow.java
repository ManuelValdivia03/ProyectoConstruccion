package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.AcademicDAO;
import logic.daos.AccountDAO;
import logic.logicclasses.Academic;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.ConsultAcademicsWindow;
import userinterface.windows.UpdateAcademicWindow;

import java.sql.SQLException;
import java.util.Objects;

public class ControllerConsultAcademicsWindow {
    private final ConsultAcademicsWindow view;
    private final AcademicDAO academicDAO;
    private final Stage currentStage;
    private final Validators validators;
    private ObservableList<Academic> allAcademics;
    private boolean hasSearchResults = false;

    public ControllerConsultAcademicsWindow(ConsultAcademicsWindow view, Stage stage) {
        this.view = Objects.requireNonNull(view, "La vista no puede ser nula");
        this.academicDAO = new AcademicDAO();
        this.currentStage = Objects.requireNonNull(stage, "El stage no puede ser nulo");
        this.validators = new Validators();
        this.allAcademics = FXCollections.observableArrayList();

        TableColumn<Academic, Void> manageCol = view.createManageButtonColumn(this::handleManageAcademic);
        view.getAcademicTable().getColumns().add(manageCol);

        setupEventHandlers();
        loadAcademicData();
    }

    private void setupEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchAcademicByStaffNumber());
        view.getSearchField().setOnAction(e -> searchAcademicByStaffNumber());
        view.getClearButton().setOnAction(e -> clearSearch());
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadAcademicData() {
        try {
            allAcademics.setAll(academicDAO.getAcademicsByStatusFromView('A'));
            view.setAcademicData(allAcademics);
            view.getSearchField().clear();
            hasSearchResults = false;
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los académicos: " + message);
        }
    }

    private void searchAcademicByStaffNumber() {
        String staffNumber = view.getSearchField().getText().trim();

        if (!validators.validateStaffNumber(staffNumber)) {
            showAlert(Alert.AlertType.WARNING, "Formato incorrecto",
                    "El número de personal debe tener 5 dígitos");
            return;
        }

        try {
            Academic academic = academicDAO.getAcademicByStaffNumber(staffNumber);
            if (academic != null && academic.getIdUser() != -1) {
                ObservableList<Academic> searchResult = FXCollections.observableArrayList();
                searchResult.add(academic);
                view.setAcademicData(searchResult);
                hasSearchResults = true;
            } else {
                view.setAcademicData(FXCollections.observableArrayList());
                hasSearchResults = false;
                showAlert(Alert.AlertType.INFORMATION, "Búsqueda",
                        "No se encontró ningún académico con el número: " + staffNumber);
            }
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error de búsqueda",
                    "Ocurrió un error al buscar el académico: " + message);
        }
    }

    private void clearSearch() {
        view.getSearchField().clear();
        if (hasSearchResults) {
            loadAcademicData();
        } else {
            view.setAcademicData(allAcademics);
        }
    }

    private void handleManageAcademic(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Academic) {
            openUpdateAcademicWindow((Academic) source);
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
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir la ventana de actualización: " + message);
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

package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.RepresentativeDAO;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import userinterface.windows.ConsultRepresentativesWindow;
import userinterface.windows.LinkOrganizationWindow;

import java.sql.SQLException;
import java.util.List;

public class ControllerConsultRepresentativesWindow {
    private final ConsultRepresentativesWindow view;
    private final RepresentativeDAO representativeDAO;
    private final LinkedOrganizationDAO organizationDAO;
    private final Stage currentStage;
    private ObservableList<Representative> allRepresentatives;
    private boolean hasSearchResults = false;

    public ControllerConsultRepresentativesWindow(ConsultRepresentativesWindow view, Stage stage) {
        this.view = view;
        this.representativeDAO = new RepresentativeDAO();
        this.organizationDAO = new LinkedOrganizationDAO();
        this.currentStage = stage;
        this.allRepresentatives = FXCollections.observableArrayList();

        TableColumn<Representative, Void> assignCol = view.createAssignButtonColumn(this::handleAssignOrganization);
        view.getRepresentativeTable().getColumns().add(assignCol);

        setupEventHandlers();
        loadRepresentativeData();
    }

    private void setupEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchRepresentativeByName());
        view.getClearButton().setOnAction(e -> clearSearch());
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadRepresentativeData() {
        try {
            List<Representative> reps = representativeDAO.getAllRepresentativesWithOrganizationName();
            allRepresentatives.setAll(reps);
            view.setRepresentativeData(allRepresentatives);
            view.getSearchField().clear();
            hasSearchResults = false;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los representantes: " + e.getMessage());
        }
    }

    private void searchRepresentativeByName() {
        String name = view.getSearchField().getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia",
                    "Por favor ingrese un nombre para buscar");
            return;
        }

        try {
            Representative rep = representativeDAO.getRepresentativeByNameWithOrganization(name);
            if (rep != null) {
                ObservableList<Representative> searchResult = FXCollections.observableArrayList();
                searchResult.add(rep);
                view.setRepresentativeData(searchResult);
                hasSearchResults = true;
            } else {
                view.setRepresentativeData(FXCollections.observableArrayList());
                hasSearchResults = false;
                showAlert(Alert.AlertType.INFORMATION, "Búsqueda",
                        "No se encontró representante con el nombre: " + name);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de búsqueda",
                    "Ocurrió un error al buscar el representante: " + e.getMessage());
        }
    }

    private void clearSearch() {
        view.getSearchField().clear();
        if (hasSearchResults) {
            loadRepresentativeData();
        } else {
            view.setRepresentativeData(allRepresentatives);
        }
    }

    private void handleAssignOrganization(ActionEvent event) {
        if (!(event.getSource() instanceof javafx.scene.control.Button)) {
            return;
        }

        javafx.scene.control.Button button = (javafx.scene.control.Button) event.getSource();
        Representative rep = (Representative) button.getUserData();

        if (rep == null) {
            return;
        }

        LinkOrganizationWindow linkWindow = new LinkOrganizationWindow(linkEvent -> {
            if (!(linkEvent.getSource() instanceof javafx.scene.control.Button)) {
                return;
            }

            javafx.scene.control.Button linkButton = (javafx.scene.control.Button) linkEvent.getSource();
            LinkedOrganization org = (LinkedOrganization) linkButton.getUserData();

            if (org == null) {
                return;
            }

            try {
                boolean updated = representativeDAO.linkRepresentativeToOrganization(
                        rep.getIdRepresentative(),
                        org.getIdLinkedOrganization()
                );
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Representante vinculado exitosamente.");
                    loadRepresentativeData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo vincular el representante.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al vincular representante: " + e.getMessage());
            }
        });

        Stage orgStage = new Stage();
        try {
            List<LinkedOrganization> orgs = organizationDAO.getAllLinkedOrganizations();
            linkWindow.setOrganizationData(FXCollections.observableArrayList(orgs));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar las organizaciones.");
        }
        linkWindow.getBackButton().setOnAction(e -> orgStage.close());

        orgStage.setScene(new Scene(linkWindow.getView(), 700, 500));
        orgStage.setTitle("Vincular Representante a Organización");
        orgStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

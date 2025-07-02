package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.RepresentativeDAO;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import logic.services.ExceptionManager;
import userinterface.windows.ConsultRepresentativesWindow;
import userinterface.windows.LinkOrganizationWindow;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ControllerConsultRepresentativesWindow {
    private final ConsultRepresentativesWindow view;
    private final RepresentativeDAO representativeDAO;
    private final LinkedOrganizationDAO organizationDAO;
    private final Stage currentStage;
    private ObservableList<Representative> allRepresentatives;
    private boolean hasSearchResults = false;

    private static final Representative EMPTY_REPRESENTATIVE = new Representative(-1, "", "", "", null);
    private static final LinkedOrganization EMPTY_ORGANIZATION = new LinkedOrganization(-1, "", "", "", "", "", ' ');

    public ControllerConsultRepresentativesWindow(ConsultRepresentativesWindow view, Stage stage) {
        this.view = Objects.requireNonNull(view, "La vista no puede ser nula");
        this.representativeDAO = new RepresentativeDAO();
        this.organizationDAO = new LinkedOrganizationDAO();
        this.currentStage = Objects.requireNonNull(stage, "El stage no puede ser nulo");
        this.allRepresentatives = FXCollections.observableArrayList();

        addAssignColumnIfNeeded();
        setupEventHandlers();
        loadRepresentativeData();
    }

    private void addAssignColumnIfNeeded() {
        boolean assignColExists = view.getRepresentativeTable().getColumns().stream()
            .anyMatch(col -> "Acción".equals(col.getText()));
        if (!assignColExists) {
            TableColumn<Representative, Void> assignCol = view.createAssignButtonColumn(this::handleAssignOrganization);
            view.getRepresentativeTable().getColumns().add(assignCol);
        }
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
                    "No se pudieron cargar los representantes: " + ExceptionManager.handleException(e));
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
                    "Ocurrió un error al buscar el representante: " + ExceptionManager.handleException(e));
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
        Representative rep = extractRepresentativeFromEvent(event);
        if (rep == EMPTY_REPRESENTATIVE) return;

        LinkOrganizationWindow linkWindow = createLinkOrganizationWindow(rep);
        Stage orgStage = new Stage();
        loadOrganizationsForLinkWindow(linkWindow);
        linkWindow.getBackButton().setOnAction(e -> orgStage.close());

        orgStage.setScene(new Scene(linkWindow.getView(), 700, 500));
        orgStage.setTitle("Vincular Representante a Organización");
        orgStage.show();
    }

    private Representative extractRepresentativeFromEvent(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button)) return EMPTY_REPRESENTATIVE;
        Button button = (Button) source;
        Representative rep = (Representative) button.getUserData();
        return rep != null ? rep : EMPTY_REPRESENTATIVE;
    }

    private LinkOrganizationWindow createLinkOrganizationWindow(Representative rep) {
        return new LinkOrganizationWindow(linkEvent -> {
            LinkedOrganization org = extractOrganizationFromEvent(linkEvent);
            if (org == EMPTY_ORGANIZATION) return;
            linkRepresentativeToOrganization(rep, org);
        });
    }

    private LinkedOrganization extractOrganizationFromEvent(ActionEvent linkEvent) {
        Object linkSource = linkEvent.getSource();
        if (!(linkSource instanceof Button)) return EMPTY_ORGANIZATION;
        Button linkButton = (Button) linkSource;
        LinkedOrganization org = (LinkedOrganization) linkButton.getUserData();
        return org != null ? org : EMPTY_ORGANIZATION;
    }

    private void linkRepresentativeToOrganization(Representative rep, LinkedOrganization org) {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Error al vincular representante: " + ExceptionManager.handleException(e));
        }
    }

    private void loadOrganizationsForLinkWindow(LinkOrganizationWindow linkWindow) {
        try {
            List<LinkedOrganization> orgs = organizationDAO.getAllLinkedOrganizations();
            linkWindow.setOrganizationData(FXCollections.observableArrayList(orgs));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar las organizaciones: " + ExceptionManager.handleException(e));
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

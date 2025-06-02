package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.LinkedOrganizationDocumentDAO;
import logic.logicclasses.LinkedOrganization;
import userinterface.windows.ConsultLinkedOrganizationsWindow;
import userinterface.windows.UpdateLinkedOrganizationWindow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;

public class ControllerConsultLinkedOrganizationsWindow {
    private final ConsultLinkedOrganizationsWindow view;
    private final LinkedOrganizationDAO organizationDAO;
    private final Stage currentStage;
    private final LinkedOrganizationDocumentDAO documentDAO;
    private ObservableList<LinkedOrganization> allOrganizations;
    private boolean hasSearchResults = false;

    public ControllerConsultLinkedOrganizationsWindow(ConsultLinkedOrganizationsWindow view, Stage stage) {
        this.view = view;
        this.documentDAO = new LinkedOrganizationDocumentDAO();
        this.organizationDAO = new LinkedOrganizationDAO();
        this.currentStage = stage;
        this.allOrganizations = FXCollections.observableArrayList();

        TableColumn<LinkedOrganization, Void> manageCol = view.createManageButtonColumn(this::handleManageOrganization);
        TableColumn<LinkedOrganization, Void> documentsCol = view.createDocumentsButtonColumn(this::handleViewDocuments);
        view.getOrganizationTable().getColumns().add(manageCol);
        view.getOrganizationTable().getColumns().add(documentsCol);

        setupEventHandlers();
        loadOrganizationData();
    }

    private void setupEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchOrganizationByName());
        view.getClearButton().setOnAction(e -> clearSearch());
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

    private void handleViewDocuments(ActionEvent event) {
        LinkedOrganization org = (LinkedOrganization) event.getSource();
        openDocumentsWindow(org);
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

    private void openDocumentsWindow(LinkedOrganization org) {
        try {
            if (documentDAO.documentExists(org.getIdLinkedOrganization())) {
                byte[] fileBytes = documentDAO.getDocumentByOrganizationId(org.getIdLinkedOrganization());
                String fileType = documentDAO.getDocumentType(org.getIdLinkedOrganization());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Documento de la Organización");
                alert.setHeaderText("Documento asociado a: " + org.getNameLinkedOrganization());

                String documentInfo = documentDAO.getDocumentInfo(org.getIdLinkedOrganization());
                alert.setContentText("Información del documento:\n" + documentInfo);

                ButtonType downloadButton = new ButtonType("Descargar", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(downloadButton, ButtonType.CLOSE);

                try {
                    InputStream iconStream = getClass().getResourceAsStream("/images/document.png");
                    if (iconStream != null) {
                        Image icon = new Image(iconStream);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(icon);
                    } else {
                        System.out.println("El ícono no se encontró en la ruta especificada");
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar el ícono: " + e.getMessage());
                }

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == downloadButton) {
                        saveDocumentToFile(fileBytes, org.getNameLinkedOrganization(), fileType);
                    }
                });
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Documentos",
                        "No hay documentos registrados para esta organización");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron recuperar los documentos: " + e.getMessage());
        }
    }

    private void saveDocumentToFile(byte[] fileBytes, String orgName, String fileType) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Documento");

        String extension = getFileExtension(fileType);
        fileChooser.setInitialFileName(
                "documento_" + orgName.replace(" ", "_") + extension
        );

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                getFileTypeDescription(fileType),
                "*" + extension
        );
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(currentStage);

        if (file != null) {
            try {
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(extension.toLowerCase())) {
                    file = new File(filePath + extension);
                }

                Files.write(file.toPath(), fileBytes);
                showAlert(Alert.AlertType.INFORMATION, "Éxito",
                        "Documento guardado como: " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "No se pudo guardar el documento: " + e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileType) {
        switch (fileType.toUpperCase()) {
            case "PDF": return ".pdf";
            case "JPG": return ".jpg";
            case "PNG": return ".png";
            case "DOC": return ".doc";
            case "DOCX": return ".docx";
            default: return ".dat";
        }
    }

    private String getFileTypeDescription(String fileType) {
        switch (fileType.toUpperCase()) {
            case "PDF": return "Documento PDF (*.pdf)";
            case "JPG": return "Imagen JPEG (*.jpg)";
            case "PNG": return "Imagen PNG (*.png)";
            case "DOC": return "Documento Word (*.doc)";
            case "DOCX": return "Documento Word (*.docx)";
            default: return "Archivo binario (*.dat)";
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

package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDocumentDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import userinterface.windows.DocumentUploadWindow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ControllerDocumentUploadWindow implements EventHandler<ActionEvent> {
    private static final Logger logger = LogManager.getLogger(ControllerDocumentUploadWindow.class);

    private final DocumentUploadWindow view;
    private final Stage stage;
    private final LinkedOrganizationDocumentDAO documentDAO;
    private final Runnable onSuccess;
    private final Runnable onCancel;
    private boolean documentUploaded = false;

    public ControllerDocumentUploadWindow(DocumentUploadWindow view, Stage stage,
                                          Runnable onSuccess, Runnable onCancel) {
        this.view = view;
        this.stage = stage;
        this.documentDAO = new LinkedOrganizationDocumentDAO();
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;

        setupEventHandlers();
        setupWindowCloseHandler();
    }

    private void setupEventHandlers() {
        view.getUploadButton().setOnAction(this);
        view.getCancelButton().setOnAction(this);
    }

    private void setupWindowCloseHandler() {
        stage.setOnCloseRequest(e -> {
            if (!documentUploaded) {
                e.consume();
                confirmCancel();
            }
        });
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getUploadButton()) {
            handleUploadDocument();
        } else if (event.getSource() == view.getCancelButton()) {
            handleCancel();
        }
    }

    private void handleUploadDocument() {
        try {
            if (view.getFileType() == null) {
                showError("Seleccione el tipo de documento");
                return;
            }

            byte[] fileBytes = view.getFileBytes();
            if (fileBytes == null) {
                showError("Seleccione un archivo");
                return;
            }

            if (documentDAO.insertDocument(
                    view.getOrganizationId(),
                    view.getFileName(),
                    view.getFileType(),
                    fileBytes)) {

                documentUploaded = true;
                closeWindow();
                onSuccess.run();
            } else {
                showError("Error al subir el documento");
            }

        } catch (IOException | SQLException e) {
            showError("Error: " + e.getMessage());
            logger.error("Error al subir documento", e);
        }
    }

    private void handleCancel() {
        confirmCancel();
    }

    private void confirmCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar cancelación");
        alert.setHeaderText("¿Está seguro que desea cancelar?");
        alert.setContentText("El registro de la organización será eliminado si no sube el documento.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            documentUploaded = false;
            closeWindow();
            onCancel.run(); // Notificar cancelación
        }
    }

    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
    }
}
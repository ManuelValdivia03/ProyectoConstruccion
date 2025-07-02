package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDocumentDAO;
import logic.services.ExceptionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import userinterface.windows.DocumentUploadWindow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Objects;

public class ControllerDocumentUploadWindow implements EventHandler<ActionEvent> {
    private static final Logger logger = LogManager.getLogger(ControllerDocumentUploadWindow.class);
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String CONFIRMATION_TITLE = "Confirmar cancelación";
    private static final String CONFIRMATION_HEADER = "¿Está seguro que desea cancelar?";
    private static final String CONFIRMATION_CONTENT = "El registro de la organización será eliminado si no sube el documento.";

    private final DocumentUploadWindow view;
    private final Stage stage;
    private final LinkedOrganizationDocumentDAO documentDAO;
    private final Runnable onSuccessCallback;
    private final Runnable onCancelCallback;
    private boolean documentUploaded = false;

    public ControllerDocumentUploadWindow(DocumentUploadWindow view, Stage stage,
                                          Runnable onSuccessCallback, Runnable onCancelCallback) {
        this.view = Objects.requireNonNull(view, "DocumentUploadWindow view cannot be null");
        this.stage = Objects.requireNonNull(stage, "Stage cannot be null");
        this.documentDAO = new LinkedOrganizationDocumentDAO();
        this.onSuccessCallback = Objects.requireNonNull(onSuccessCallback, "Success callback cannot be null");
        this.onCancelCallback = Objects.requireNonNull(onCancelCallback, "Cancel callback cannot be null");

        initializeController();
    }

    private void initializeController() {
        setupEventHandlers();
        setupWindowCloseHandler();
    }

    private void setupEventHandlers() {
        view.getUploadButton().setOnAction(this);
        view.getCancelButton().setOnAction(this);
    }

    private void setupWindowCloseHandler() {
        stage.setOnCloseRequest(event -> {
            if (!documentUploaded) {
                event.consume();
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
            String fileName = view.getFileName();
            String selectedExtension = view.getFileType();

            if (validateFileExtension(fileName, selectedExtension)) {
                boolean uploadSuccessful = documentDAO.insertDocument(
                        view.getOrganizationId(),
                        fileName,
                        selectedExtension,
                        view.getFileBytes()
                );
                handleUploadResult(uploadSuccessful);
            }
        } catch (IOException | SQLException e) {
            String message = ExceptionManager.handleException(e);
            handleUploadError(new Exception(message, e));
        }
    }

    private boolean validateFileExtension(String fileName, String selectedExtension) {
        boolean isValid = true;
        if (fileName == null || selectedExtension == null) {
            showError("El archivo o la extensión seleccionada no pueden ser nulos");
            isValid = false;
        }

        String fileExtension = fileName.toLowerCase();
        selectedExtension = selectedExtension.toLowerCase();

        switch (selectedExtension) {
            case "pdf":
                if (!fileExtension.endsWith(".pdf")) {
                    showError("El archivo debe ser de tipo PDF");
                    isValid = false;
                }
                break;
            case "doc":
            case "docx":
                if (!fileExtension.endsWith(".doc") && !fileExtension.endsWith(".docx")) {
                    showError("El archivo debe ser de tipo DOC o DOCX");
                    isValid = false;
                }
                break;
            case "jpg":
            case "jpeg":
                if (!fileExtension.endsWith(".jpg") && !fileExtension.endsWith(".jpeg")) {
                    showError("El archivo debe ser de tipo JPG o JPEG");
                    isValid = false;
                }
                break;
            case "png":
                if (!fileExtension.endsWith(".png")) {
                    showError("El archivo debe ser de tipo PNG");
                    isValid = false;
                }
                break;
            default:
                showError("Tipo de archivo no soportado");
                isValid = false;
        }

        return isValid;
    }

    private void handleUploadResult(boolean uploadSuccessful) {
        if (uploadSuccessful) {
            documentUploaded = true;
            closeWindow();
            onSuccessCallback.run();
        } else {
            showError("Error al subir el documento");
        }
    }

    private void handleUploadError(Exception e) {
        logger.error("Error uploading document", e);
        showError("Error: " + e.getMessage());
    }

    private void handleCancel() {
        confirmCancel();
    }

    private void confirmCancel() {
        Alert confirmationDialog = createConfirmationDialog();
        Optional<ButtonType> result = confirmationDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            documentUploaded = false;
            closeWindow();
            onCancelCallback.run();
        }
    }

    private Alert createConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(CONFIRMATION_TITLE);
        alert.setHeaderText(CONFIRMATION_HEADER);
        alert.setContentText(CONFIRMATION_CONTENT);
        return alert;
    }

    private void closeWindow() {
        stage.close();
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle(ERROR_TEXT_STYLE);
    }
}
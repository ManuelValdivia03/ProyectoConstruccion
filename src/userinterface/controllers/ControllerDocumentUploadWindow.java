package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.LinkedOrganizationDocumentDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import userinterface.windows.DocumentUploadWindow;

import java.io.IOException;
import java.sql.SQLException;

public class ControllerDocumentUploadWindow implements EventHandler<ActionEvent> {
    private static final Logger logger = LogManager.getLogger(ControllerDocumentUploadWindow.class);

    private final DocumentUploadWindow view;
    private final LinkedOrganizationDocumentDAO documentDAO;

    public ControllerDocumentUploadWindow(DocumentUploadWindow view) {
        this.view = view;
        this.documentDAO = new LinkedOrganizationDocumentDAO();
        setupEventHandlers();
        logger.debug("Controlador de subida de documentos creado para organización ID: {}", view.getOrganizationId());
    }

    private void setupEventHandlers() {
        view.getUploadButton().setOnAction(this);
        view.getCancelButton().setOnAction(this);
        logger.debug("Manejadores de eventos configurados");
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
            logger.debug("Iniciando proceso de subida de documento");

            if (view.getFileType() == null) {
                showError("Seleccione el tipo de documento");
                logger.warn("Intento de subida sin seleccionar tipo de documento");
                return;
            }

            byte[] fileBytes = view.getFileBytes();
            if (fileBytes == null) {
                showError("Seleccione un archivo");
                logger.warn("Intento de subida sin archivo seleccionado");
                return;
            }

            if (fileBytes.length > 10_000_000) { // 10MB
                showError("El archivo excede el tamaño máximo permitido (10MB)");
                logger.warn("Intento de subida con archivo demasiado grande: {} bytes", fileBytes.length);
                return;
            }

            if (documentDAO.insertDocument(
                    view.getOrganizationId(),
                    view.getFileName(),
                    view.getFileType(),
                    fileBytes)) {

                showSuccess("Documento subido exitosamente");
                logger.info("Documento subido correctamente para organización ID: {}. Archivo: {}",
                        view.getOrganizationId(), view.getFileName());

                closeWindow();
            } else {
                showError("Error al subir el documento");
                logger.error("Fallo al subir documento para organización ID: {}", view.getOrganizationId());
            }

        } catch (IOException e) {
            showError("Error al leer el archivo: " + e.getMessage());
            logger.error("Error de IO al subir documento", e);
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
            logger.error("Error de SQL al subir documento", e);
        }
    }

    private void handleCancel() {
        logger.debug("Operación cancelada por el usuario");
        closeWindow();
    }

    private void closeWindow() {
        if (view.getView().getScene() != null && view.getView().getScene().getWindow() != null) {
            view.getView().getScene().getWindow().hide();
        }
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
    }

    private void showSuccess(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #4CAF50;");
    }
}
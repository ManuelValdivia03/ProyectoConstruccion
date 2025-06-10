package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DocumentUploadWindow {
    private final VBox view;
    private final Button selectFileButton;
    private final Button uploadButton;
    private final Button cancelButton;
    private final Label fileNameLabel;
    private final Label resultLabel;
    private final ComboBox<String> fileTypeComboBox;
    private File selectedFile;
    private final int organizationId;

    public DocumentUploadWindow(int organizationId) {
        this.organizationId = organizationId;

        Label titleLabel = new Label("Subir Documento Justificativo");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        fileTypeComboBox = new ComboBox<>();
        fileTypeComboBox.getItems().addAll("PDF", "JPG", "PNG", "DOC", "DOCX");
        fileTypeComboBox.setPromptText("Seleccione tipo de archivo");

        selectFileButton = new Button("Seleccionar Archivo");
        uploadButton = new Button("Subir Documento");
        uploadButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        fileNameLabel = new Label("Ningún archivo seleccionado");
        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Tipo de documento:"), 0, 0);
        grid.add(fileTypeComboBox, 1, 0);
        grid.add(selectFileButton, 0, 1, 2, 1);
        grid.add(fileNameLabel, 0, 2, 2, 1);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, uploadButton, cancelButton, resultLabel);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        selectFileButton.setOnAction(event -> handleSelectFile());
    }

    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Documento Justificativo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documentos", "*.pdf", "*.jpg", "*.jpeg", "*.png", "*.doc", "*.docx")
        );

        selectedFile = fileChooser.showOpenDialog(view.getScene().getWindow());
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
        }
    }

    public byte[] getFileBytes() throws IOException {
        if (selectedFile != null) {
            return Files.readAllBytes(selectedFile.toPath());
        }
        return null;
    }

    public String getFileType() {
        return fileTypeComboBox.getValue();
    }

    public String getFileName() {
        return selectedFile != null ? selectedFile.getName() : null;
    }

    public VBox getView() { return view; }
    public Button getUploadButton() { return uploadButton; }
    public Button getCancelButton() { return cancelButton; }
    public Label getResultLabel() { return resultLabel; }
    public int getOrganizationId() { return organizationId; }
}
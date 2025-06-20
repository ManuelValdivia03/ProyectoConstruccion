package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class AddActivityWindow {
    private final GridPane view;
    private final TextField nameField;
    private final TextArea descriptionArea;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final ComboBox<String> statusComboBox;
    private final Button saveButton;
    private final Button cancelButton;
    private final Label statusLabel;

    public AddActivityWindow() {
        nameField = new TextField();
        nameField.setPromptText("Nombre de la actividad");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descripción");
        descriptionArea.setPrefRowCount(3);

        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Fecha de inicio");

        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Fecha de término");

        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Pendiente", "En progreso", "Completada");
        statusComboBox.setValue("Pendiente");

        saveButton = new Button("Guardar");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        view = new GridPane();
        view.setHgap(10);
        view.setVgap(10);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.CENTER);

        view.add(new Label("Nombre:"), 0, 0);
        view.add(nameField, 1, 0);
        view.add(new Label("Descripción:"), 0, 1);
        view.add(descriptionArea, 1, 1);
        view.add(new Label("Fecha inicio:"), 0, 2);
        view.add(startDatePicker, 1, 2);
        view.add(new Label("Fecha término:"), 0, 3);
        view.add(endDatePicker, 1, 3);
        view.add(new Label("Estado:"), 0, 4);
        view.add(statusComboBox, 1, 4);

        HBox buttonsBox = new HBox(10, saveButton, cancelButton);
        buttonsBox.setAlignment(Pos.CENTER);
        view.add(buttonsBox, 0, 5, 2, 1);

        view.add(statusLabel, 0, 6, 2, 1);
    }

    public GridPane getView() {
        return view;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextArea getDescriptionArea() {
        return descriptionArea;
    }

    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }

    public ComboBox<String> getStatusComboBox() {
        return statusComboBox;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setText(message);
    }
}
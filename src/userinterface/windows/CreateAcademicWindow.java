package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import logic.enums.AcademicType;

public class CreateAcademicWindow {
    private VBox view;
    private TextField nameField, phoneField, staffNumberField;
    private ComboBox<String> typeComboBox;
    private Button addButton;
    private Label resultLabel;

    public CreateAcademicWindow() {
        createView();
    }

    private void createView() {
        Label title = new Label("Registra academico");

        Label nameLabel = new Label("Nombre Completo:");
        nameField = new TextField();

        Label phoneLabel = new Label("Teléfono:");
        phoneField = new TextField();

        Label staffNumberLabel = new Label("Numero de personal:");
        staffNumberField = new TextField();

        Label statusLabel = new Label("Tipo de academico:");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Evaluador", "EE");
        typeComboBox.setValue("Evaluador");

        addButton = new Button("Agregar Usuario");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: #0066cc;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(phoneLabel, 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(staffNumberLabel, 0, 2);
        grid.add(staffNumberField, 1, 2);
        grid.add(statusLabel, 0, 3);
        grid.add(typeComboBox, 1, 3);

        view = new VBox(15);
        view.setPadding(new Insets(10));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(grid, addButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getPhoneField() {
        return phoneField;
    }

    public TextField getStaffNumberField() {
        return staffNumberField;
    }

    public ComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }
}
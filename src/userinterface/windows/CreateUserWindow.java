package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CreateUserWindow {
    private VBox view;
    private TextField nameField;
    private TextField phoneField;
    private ComboBox<String> statusCombo;
    private Button addButton;
    private Label resultLabel;

    public CreateUserWindow() {
        createView();
    }

    private void createView() {
        Label nameLabel = new Label("Nombre Completo:");
        nameField = new TextField();

        Label phoneLabel = new Label("Teléfono:");
        phoneField = new TextField();

        Label statusLabel = new Label("Estado:");
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("A - Activo", "I - Inactivo");
        statusCombo.setValue("A - Activo");

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
        grid.add(statusLabel, 0, 2);
        grid.add(statusCombo, 1, 2);

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

    public ComboBox<String> getStatusCombo() {
        return statusCombo;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }
}
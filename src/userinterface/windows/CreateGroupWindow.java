package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CreateGroupWindow {
    private final VBox view;
    private final TextField nrcField;
    private final TextField groupNameField;
    private final Button addButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public CreateGroupWindow() {
        Label titleLabel = new Label("Registrar Grupo");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nrcField = new TextField();
        groupNameField = new TextField();

        addButton = new Button("Agregar Grupo");
        addButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("NRC:"), 0, 0);
        grid.add(nrcField, 1, 0);
        grid.add(new Label("Nombre del Grupo:"), 0, 1);
        grid.add(groupNameField, 1, 1);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, addButton, cancelButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public TextField getNrcField() {
        return nrcField;
    }

    public TextField getGroupNameField() {
        return groupNameField;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }
}

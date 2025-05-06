package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class RegistProyectWindow {
    private final VBox view;
    private final TextField titleTextField;
    private final TextField descriptionTextField;
    private final TextField dateStartTextField;
    private final TextField dateEndTextField;
    private final Button registerButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public RegistProyectWindow() {
        Label titleLabel = new Label("Registrar Proyecto");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        titleTextField = new TextField();
        descriptionTextField = new TextField();
        dateStartTextField = new TextField();
        dateStartTextField.setPromptText("YYYY-MM-DD");
        dateEndTextField = new TextField();
        dateEndTextField.setPromptText("YYYY-MM-DD");
        registerButton = new Button("Registrar Proyecto");
        registerButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(25, 25, 25, 25));

        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleTextField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descriptionTextField, 1, 1);
        grid.add(new Label("Fecha de Inicio:"), 0, 2);
        grid.add(dateStartTextField, 1, 2);
        grid.add(new Label("Fecha de Fin:"), 0, 3);
        grid.add(dateEndTextField, 1, 3);

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, registerButton, cancelButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public TextField getTitleTextField() {
        return titleTextField;
    }

    public TextField getDescriptionTextField() {
        return descriptionTextField;
    }

    public TextField getDateStartTextField() {
        return dateStartTextField;
    }

    public TextField getDateEndTextField() {
        return dateEndTextField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }
}

package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class RegistRepresentativeWindow {
    private final VBox view;
    private final TextField nameTextField;
    private final TextField emailTextField;
    private final TextField phoneTextField;
    private final Button registerButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public RegistRepresentativeWindow() {
        Label titleLabel = new Label("Registrar Representante");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameTextField = new TextField();
        emailTextField = new TextField();
        phoneTextField = new TextField();

        registerButton = new Button("Registrar Representante");
        registerButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Nombre completo:"), 0, 0);
        grid.add(nameTextField, 1, 0);
        grid.add(new Label("Correo electrónico:"), 0, 1);
        grid.add(emailTextField, 1, 1);
        grid.add(new Label("Teléfono:"), 0, 2);
        grid.add(phoneTextField, 1, 2);

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, registerButton, cancelButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public TextField getNameTextField() {
        return nameTextField;
    }

    public TextField getEmailTextField() {
        return emailTextField;
    }

    public TextField getPhoneTextField() {
        return phoneTextField;
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

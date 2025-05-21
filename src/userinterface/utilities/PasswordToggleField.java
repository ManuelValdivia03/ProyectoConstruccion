package userinterface.utilities;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

public class PasswordToggleField {
    private final PasswordField passwordField;
    private final TextField visibleField;
    private final Button toggleButton;

    public PasswordToggleField() {
        passwordField = new PasswordField();
        passwordField.setStyle("-fx-pref-width: 200px;");

        visibleField = new TextField();
        visibleField.setStyle("-fx-pref-width: 200px;");
        visibleField.setVisible(false);

        toggleButton = new Button("👁");
        toggleButton.setStyle("-fx-background-color: transparent; -fx-padding: 0 5 0 5;");
        toggleButton.setTooltip(new Tooltip("Mostrar contraseña"));

        passwordField.textProperty().bindBidirectional(visibleField.textProperty());

        toggleButton.setOnAction(e -> toggleVisibility());
    }

    private void toggleVisibility() {
        boolean show = passwordField.isVisible();
        passwordField.setVisible(!show);
        visibleField.setVisible(show);
        toggleButton.setText(show ? "👁" : "🚫");
        toggleButton.setTooltip(new Tooltip(show ? "Mostrar contraseña" : "Ocultar contraseña"));
    }

    public HBox getContainer() {
        StackPane fieldsPane = new StackPane(passwordField, visibleField);
        HBox container = new HBox(5, fieldsPane, toggleButton);
        container.setAlignment(Pos.CENTER_LEFT);
        return container;
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public void clear() {
        passwordField.clear();
    }

}
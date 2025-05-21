package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import userinterface.utilities.PasswordToggleField;

public class RecoveryPasswordWindow {
    private final GridPane view;
    private final PasswordToggleField newPasswordField;
    private final PasswordToggleField confirmPasswordField;
    private final Button submitButton;
    private final Label messageLabel;

    public RecoveryPasswordWindow() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        Text title = new Text("Establecer Nueva Contraseña");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        grid.add(title, 0, 0, 2, 1);

        Label newPasswordLabel = new Label("Nueva Contraseña:");
        grid.add(newPasswordLabel, 0, 1);

        newPasswordField = new PasswordToggleField();
        grid.add(newPasswordField.getContainer(), 1, 1);

        Label confirmPasswordLabel = new Label("Confirmar Contraseña:");
        grid.add(confirmPasswordLabel, 0, 2);

        confirmPasswordField = new PasswordToggleField();
        grid.add(confirmPasswordField.getContainer(), 1, 2);

        submitButton = new Button("Actualizar Contraseña");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        grid.add(submitButton, 1, 3);

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #d32f2f;");
        grid.add(messageLabel, 0, 4, 2, 1);

        this.view = grid;
    }

    public GridPane getView() {
        return view;
    }

    public String getNewPassword() {
        return newPasswordField.getPassword();
    }

    public String getConfirmedPassword() {
        return confirmPasswordField.getPassword();
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public boolean validatePasswords() {
        if (getNewPassword().isEmpty() || getConfirmedPassword().isEmpty()) {
            messageLabel.setText("Ambos campos son requeridos");
            return false;
        }

        if (!getNewPassword().equals(getConfirmedPassword())) {
            messageLabel.setText("Las contraseñas no coinciden");
            return false;
        }

        if (getNewPassword().length() < 8) {
            messageLabel.setText("La contraseña debe tener al menos 8 caracteres");
            return false;
        }

        messageLabel.setText("");
        return true;
    }
}
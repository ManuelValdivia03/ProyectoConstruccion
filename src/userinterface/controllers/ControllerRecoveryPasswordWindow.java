package userinterface.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logic.services.PasswordRecoveryService;
import logic.exceptions.UserNotFoundException;

public class ControllerRecoveryPasswordWindow {
    private final PasswordRecoveryService recoveryService;
    private final Stage stage;

    // Campos de la primera pantalla
    private final TextField emailField = new TextField();
    private final TextField identifierField = new TextField();
    private final Label identifierLabel = new Label("Matrícula o Número de Personal:");
    private final Button nextButton = new Button("Siguiente");
    private final Label messageLabel = new Label();

    // Campos de la segunda pantalla (contraseña)
    private final PasswordField newPasswordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();
    private final Button submitButton = new Button("Actualizar Contraseña");
    private final Label passwordMessageLabel = new Label();

    private String validatedEmail;

    public ControllerRecoveryPasswordWindow(Stage stage, PasswordRecoveryService recoveryService) {
        this.stage = stage;
        this.recoveryService = recoveryService;
        showEmailAndIdentifierForm();
    }

    private void showEmailAndIdentifierForm() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        Label title = new Label("Recuperar Contraseña");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        grid.add(title, 0, 0, 2, 1);

        grid.add(new Label("Correo electrónico:"), 0, 1);
        grid.add(emailField, 1, 1);

        grid.add(identifierLabel, 0, 2);
        grid.add(identifierField, 1, 2);

        nextButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;");
        grid.add(nextButton, 1, 3);

        messageLabel.setStyle("-fx-text-fill: #d32f2f;");
        grid.add(messageLabel, 0, 4, 2, 1);

        nextButton.setOnAction(e -> handleValidateUser());

        stage.setScene(new Scene(grid));
        stage.show();
    }

    private void handleValidateUser() {
        String email = emailField.getText().trim();
        String identifier = identifierField.getText().trim();

        if (email.isEmpty() || identifier.isEmpty()) {
            messageLabel.setText("Todos los campos son obligatorios.");
            return;
        }

        try {
            boolean valid = recoveryService.validateUser(email, identifier);
            if (valid) {
                validatedEmail = email;
                showPasswordForm();
            }
        } catch (UserNotFoundException ex) {
            messageLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            messageLabel.setText("Error de conexión o inesperado.");
        }
    }

    private void showPasswordForm() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        Label title = new Label("Establecer Nueva Contraseña");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        grid.add(title, 0, 0, 2, 1);

        grid.add(new Label("Nueva Contraseña:"), 0, 1);
        grid.add(newPasswordField, 1, 1);

        grid.add(new Label("Confirmar Contraseña:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        grid.add(submitButton, 1, 3);

        passwordMessageLabel.setStyle("-fx-text-fill: #d32f2f;");
        grid.add(passwordMessageLabel, 0, 4, 2, 1);

        submitButton.setOnAction(e -> handleUpdatePassword());

        stage.setScene(new Scene(grid));
        stage.show();
    }

    private void handleUpdatePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            passwordMessageLabel.setText("Ambos campos son requeridos.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            passwordMessageLabel.setText("Las contraseñas no coinciden.");
            return;
        }
        if (newPassword.length() < 8) {
            passwordMessageLabel.setText("La contraseña debe tener al menos 8 caracteres.");
            return;
        }

        try {
            recoveryService.updatePassword(validatedEmail, newPassword);
            passwordMessageLabel.setStyle("-fx-text-fill: #388e3c;");
            passwordMessageLabel.setText("Contraseña actualizada exitosamente.");
            submitButton.setDisable(true);
        } catch (Exception ex) {
            passwordMessageLabel.setStyle("-fx-text-fill: #d32f2f;");
            passwordMessageLabel.setText("Error al actualizar la contraseña.");
        }
    }
}

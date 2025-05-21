package userinterface.windows;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import userinterface.utilities.PasswordToggleField;

public class LoginWindow {
    private final GridPane view;
    private final TextField emailField;
    private final PasswordToggleField passwordField;
    private final Button loginButton;
    private final Button exitButton;
    private final Label messageLabel;
    private final Hyperlink recoveryPasswordLink;

    public LoginWindow() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(30, 30, 30, 30));
        grid.setStyle("-fx-background-color: #f8f9fa;");

        Text title = new Text("Inicio de Sesión");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-fill: #2c3e50;");
        grid.add(title, 0, 0, 2, 1);
        GridPane.setHalignment(title, javafx.geometry.HPos.CENTER);
        GridPane.setMargin(title, new Insets(0, 0, 20, 0));

        Label emailLabel = new Label("Correo electrónico:");
        grid.add(emailLabel, 0, 1);

        emailField = new TextField();
        emailField.setPromptText("usuario@dominio.com");
        emailField.setStyle("-fx-pref-width: 250px;");
        grid.add(emailField, 1, 1);

        Label passwordLabel = new Label("Contraseña:");
        grid.add(passwordLabel, 0, 2);

        passwordField = new PasswordToggleField();
        grid.add(passwordField.getContainer(), 1, 2);

        loginButton = new Button("Ingresar");
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        loginButton.setPrefWidth(100);

        exitButton = new Button("Salir");
        exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        exitButton.setPrefWidth(100);

        HBox buttonsBox = new HBox(20, loginButton, exitButton);
        buttonsBox.setAlignment(Pos.CENTER);
        grid.add(buttonsBox, 0, 3, 2, 1);
        GridPane.setMargin(buttonsBox, new Insets(20, 0, 0, 0));

        recoveryPasswordLink = new Hyperlink("¿Olvidaste tu contraseña?");
        recoveryPasswordLink.setStyle("-fx-text-fill: #1976D2; -fx-border-color: transparent;");
        grid.add(recoveryPasswordLink, 0, 4, 2, 1);
        GridPane.setMargin(recoveryPasswordLink, new Insets(10, 0, 0, 0));
        GridPane.setHalignment(recoveryPasswordLink, HPos.CENTER);

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        grid.add(messageLabel, 0, 5, 2, 1);
        GridPane.setHalignment(messageLabel, javafx.geometry.HPos.CENTER);

        this.view = grid;
    }


    public GridPane getView() { return view; }
    public TextField getEmailField() { return emailField; }
    public PasswordToggleField getPasswordField() { return passwordField; }
    public Button getLoginButton() { return loginButton; }
    public Button getExitButton() { return exitButton; }
    public Label getMessageLabel() { return messageLabel; }
    public Hyperlink getRecoveryPasswordLink() {
        return recoveryPasswordLink;
    }
}

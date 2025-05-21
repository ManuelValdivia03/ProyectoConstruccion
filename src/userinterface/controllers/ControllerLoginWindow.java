package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import logic.services.LoginService;
import logic.services.PasswordRecoveryService;
import logic.logicclasses.User;
import userinterface.windows.LoginWindow;
import userinterface.windows.RecoveryPasswordWindow;

public class ControllerLoginWindow implements EventHandler<ActionEvent> {
    private final LoginWindow view;
    private final LoginService loginService;
    private final LoginSuccessHandler successHandler;
    private final Runnable exitHandler;

    public interface LoginSuccessHandler {
        void onLoginSuccess(User user);
    }

    public ControllerLoginWindow(LoginWindow view, LoginService loginService,
                                 LoginSuccessHandler successHandler, Runnable exitHandler,
                                 PasswordRecoveryService recoveryService) {
        this.view = view;
        this.loginService = loginService;
        this.successHandler = successHandler;
        this.exitHandler = exitHandler;

        setupEventHandlers();

        view.getRecoveryPasswordLink().setOnAction(e -> {
            Stage recoveryStage = new Stage();
            recoveryStage.setTitle("Recuperar Contraseña");
            new ControllerRecoveryPasswordWindow(recoveryStage, recoveryService);
        });
    }

    private void setupEventHandlers() {
        view.getLoginButton().setOnAction(this);
        view.getExitButton().setOnAction(e -> exitHandler.run());
    }

    @Override
    public void handle(ActionEvent event) {
        String email = view.getEmailField().getText().trim();
        String password = view.getPasswordField().getPassword();

        if (email.isEmpty() || password.isEmpty()) {
            view.getMessageLabel().setText("Por favor complete todos los campos");
            return;
        }

        User user = loginService.login(email, password);

        if (user != null) {
            view.getMessageLabel().setText("¡Ingreso exitoso!");
            view.getMessageLabel().setStyle("-fx-text-fill: #27ae60;");

            if (successHandler != null) {
                successHandler.onLoginSuccess(user);
            }
        } else {
            view.getMessageLabel().setText("Credenciales inválidas");
            view.getMessageLabel().setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    public void setRecoveryPasswordHandler(EventHandler<ActionEvent> handler) {
        view.getRecoveryPasswordLink().setOnAction(handler);
    }

    public void enableDefaultRecoveryPasswordFlow(PasswordRecoveryService recoveryService) {
        view.getRecoveryPasswordLink().setOnAction(e -> {
            Stage recoveryStage = new Stage();
            recoveryStage.setTitle("Recuperar Contraseña");
            new ControllerRecoveryPasswordWindow(recoveryStage, recoveryService);
        });
    }
}

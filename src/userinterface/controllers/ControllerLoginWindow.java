package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import logic.exceptions.InvalidCredentialsException;
import logic.logicclasses.Academic;
import logic.logicclasses.Coordinator;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import logic.services.LoginService;
import logic.services.PasswordRecoveryService;
import userinterface.windows.LoginWindow;
import java.sql.SQLException;
import java.util.Objects;

public class ControllerLoginWindow implements EventHandler<ActionEvent> {
    private static final String SUCCESS_COLOR = "-fx-text-fill: #27ae60;";
    private static final String ERROR_COLOR = "-fx-text-fill: #e74c3c;";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;

    private final LoginWindow view;
    private final LoginService loginService;
    private final PasswordRecoveryService recoveryService;
    private final Stage primaryStage;

    public ControllerLoginWindow(Stage primaryStage, LoginWindow view,
                                 LoginService loginService,
                                 PasswordRecoveryService recoveryService) {
        this.primaryStage = Objects.requireNonNull(primaryStage, "Primary stage no puede ser nulo");
        this.view = Objects.requireNonNull(view, "La vista de inicio de sesión no puede ser nula");
        this.loginService = Objects.requireNonNull(loginService, "El servicio de inicio de sesión no puede ser nulo");
        this.recoveryService = Objects.requireNonNull(recoveryService, "El servicio de recuperación de contraseña no puede ser nulo");

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getLoginButton().setOnAction(this);
        view.getExitButton().setOnAction(e -> primaryStage.close());
        view.getRecoveryPasswordLink().setOnAction(this::handlePasswordRecovery);
    }

    private void handlePasswordRecovery(ActionEvent event) {
        Stage recoveryStage = new Stage();
        recoveryStage.setTitle("Recuperar Contraseña");
        new ControllerRecoveryPasswordWindow(recoveryStage, recoveryService);
    }

    @Override
    public void handle(ActionEvent event) {
        String email = view.getEmailField().getText().trim();
        String password = view.getPasswordField().getPassword();

        if (!areFieldsValid(email, password)) {
            return;
        }

        try {
            User user = loginService.login(email, password);
            handleSuccessfulLogin(user);
        } catch (InvalidCredentialsException e) {
            showMessage("Credenciales inválidas", ERROR_COLOR);
        } catch (SQLException e) {
            showMessage("Error al conectar con la base de datos", ERROR_COLOR);
        }
    }

    private boolean areFieldsValid(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Por favor complete todos los campos", ERROR_COLOR);
            return false;
        }
        return true;
    }

    private void handleSuccessfulLogin(User user) {
        showMessage("¡Ingreso exitoso!", SUCCESS_COLOR);

        primaryStage.close();
        Stage userStage = new Stage();

        if (user instanceof Coordinator) {
            new ControllerCoordinatorMenuWindow(userStage, (Coordinator) user, createLogoutHandler());
        } else if (user instanceof Student) {
            new ControllerStudentMenuWindow(userStage, (Student) user, createLogoutHandler());
        } else if (user instanceof Academic) {
            new ControllerAcademicMenuWindow(userStage, (Academic) user, createLogoutHandler());
        }
    }

    private Runnable createLogoutHandler() {
        return () -> {
            view.clearFields();
            launchNewLoginWindow();
        };
    }

    private void showMessage(String message, String style) {
        view.getMessageLabel().setText(message);
        view.getMessageLabel().setStyle(style);
    }

    private void launchNewLoginWindow() {
        Stage newLoginStage = new Stage();
        LoginWindow newLoginView = new LoginWindow();

        ControllerLoginWindow newController = new ControllerLoginWindow(
                newLoginStage,
                newLoginView,
                loginService,
                recoveryService
        );

        newLoginStage.setScene(new Scene(newLoginView.getView(), WINDOW_WIDTH, WINDOW_HEIGHT));
        newLoginStage.setTitle("Inicio de Sesión");
        newLoginStage.setResizable(false);
        newLoginStage.show();
    }
}
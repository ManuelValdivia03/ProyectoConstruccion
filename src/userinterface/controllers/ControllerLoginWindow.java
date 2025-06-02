package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.logicclasses.Academic;
import logic.logicclasses.Coordinator;
import logic.logicclasses.Student;
import logic.services.LoginService;
import logic.services.PasswordRecoveryService;
import logic.logicclasses.User;
import userinterface.windows.LoginWindow;

public class ControllerLoginWindow implements EventHandler<ActionEvent> {
    private final LoginWindow view;
    private final LoginService loginService;
    private final PasswordRecoveryService recoveryService;
    private final Stage primaryStage;

    public ControllerLoginWindow(Stage primaryStage, LoginWindow view,
                                 LoginService loginService,
                                 PasswordRecoveryService recoveryService) {
        this.primaryStage = primaryStage;
        this.view = view;
        this.loginService = loginService;
        this.recoveryService = recoveryService;

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getLoginButton().setOnAction(this);
        view.getExitButton().setOnAction(e -> primaryStage.close());

        view.getRecoveryPasswordLink().setOnAction(e -> {
            Stage recoveryStage = new Stage();
            recoveryStage.setTitle("Recuperar Contraseña");
            new ControllerRecoveryPasswordWindow(recoveryStage, recoveryService);
        });
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

            if (user instanceof Coordinator) {
                primaryStage.close();

                Stage coordinatorStage = new Stage();
                new ControllerCoordinatorMenuWindow(coordinatorStage, (Coordinator) user, () -> {
                    coordinatorStage.close();
                    view.clearFields();
                    launchNewLoginWindow();
                });
            } else if (user instanceof Student) {
                primaryStage.close();
                Stage studentStage = new Stage();

                new ControllerStudentMenuWindow(studentStage, (Student) user, () -> {
                    studentStage.close();
                    view.clearFields();
                    launchNewLoginWindow();
                });
            } else if (user instanceof Academic) {
                primaryStage.close();
                Stage academicStage = new Stage();
                new ControllerAcademicMenuWindow(academicStage, (Academic) user, () -> {
                    academicStage.close();
                    view.clearFields();
                    launchNewLoginWindow();
                });
            }
        } else {
            view.getMessageLabel().setText("Credenciales inválidas");
            view.getMessageLabel().setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void launchNewLoginWindow() {
        Stage newLoginStage = new Stage();
        LoginWindow newLoginView = new LoginWindow();
        new ControllerLoginWindow(
                newLoginStage,
                newLoginView,
                this.loginService,
                this.recoveryService
        );
        newLoginStage.setScene(new Scene(newLoginView.getView(), 600, 400));
        newLoginStage.setTitle("Inicio de Sesión");
        newLoginStage.show();
    }
}
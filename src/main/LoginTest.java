package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.*;
import logic.services.LoginService;
import logic.services.PasswordRecoveryService;
import userinterface.controllers.ControllerLoginWindow;
import userinterface.windows.LoginWindow;

public class LoginTest extends Application {
    @Override
    public void start(Stage primaryStage) {
        AccountDAO accountDAO = new AccountDAO();
        CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
        AcademicDAO academicDAO = new AcademicDAO();
        StudentDAO studentDAO = new StudentDAO();

        LoginService loginService = new LoginService(
                accountDAO, coordinatorDAO, academicDAO, studentDAO
        );

        PasswordRecoveryService recoveryService = new PasswordRecoveryService(
                accountDAO, coordinatorDAO, academicDAO, studentDAO
        );

        LoginWindow loginWindow = new LoginWindow();
        Scene scene = new Scene(loginWindow.getView(), 600, 400);

        ControllerLoginWindow loginController = new ControllerLoginWindow(
                primaryStage,
                loginWindow,
                loginService,
                recoveryService
        );

        primaryStage.setTitle("Sistema de Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

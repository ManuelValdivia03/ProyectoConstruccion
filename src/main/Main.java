package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.windows.CreateUserWindow;
import userinterface.controllers.ControllerCreateUserWindow;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        CreateUserWindow userView = new CreateUserWindow();
        ControllerCreateUserWindow controller = new ControllerCreateUserWindow(userView);

        Scene scene = new Scene(userView.getView(), 400, 300);
        primaryStage.setTitle("Gestión de Usuarios");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerConsultAcademicsWindow;
import userinterface.controllers.ControllerCreateStudentWindow;
import userinterface.windows.ConsultAcademicsWindow;
import userinterface.windows.CreateStudentWindow;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        ConsultAcademicsWindow window = new ConsultAcademicsWindow();
        Stage academicListStage = new Stage();

        Scene scene = new Scene(window.getView(), 800, 600);
        academicListStage.setScene(scene);
        academicListStage.setTitle("Lista de Académicos");

        new ControllerConsultAcademicsWindow(window, academicListStage);

        academicListStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
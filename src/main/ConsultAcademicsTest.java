package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerConsultAcademicsWindow;
import userinterface.windows.ConsultAcademicsWindow;


public class ConsultAcademicsTest extends Application {

    public void start (Stage primaryStage) {
        ConsultAcademicsWindow window = new ConsultAcademicsWindow();
        Stage academicListStage = new Stage();

        Scene scene = new Scene(window.getView(), 650, 420);
        academicListStage.setScene(scene);
        academicListStage.setTitle("Lista de Académicos");

        new ControllerConsultAcademicsWindow(window, academicListStage);

        academicListStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

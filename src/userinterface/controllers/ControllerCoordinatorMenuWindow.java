package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.logicclasses.Coordinator;
import userinterface.windows.CoordinatorMenuWindow;

public class ControllerCoordinatorMenuWindow implements EventHandler<ActionEvent> {
    private final CoordinatorMenuWindow view;
    private final Stage stage;
    private final Runnable onLogout;

    public ControllerCoordinatorMenuWindow(Stage stage, Coordinator coordinator, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        this.view = new CoordinatorMenuWindow(coordinator);

        setupEventHandlers();
        initializeStage();
    }

    private void setupEventHandlers() {
        view.getLogoutButton().setOnAction(e -> {
            onLogout.run();
            stage.close();
        });
    }

    private void initializeStage() {
        stage.setScene(new Scene(view.getView(), 1024, 768));
        stage.setTitle("Sistema de Coordinación - Universidad");
        stage.show();
    }

    @Override
    public void handle(ActionEvent event) {
        // Manejo de eventos específicos
    }
}
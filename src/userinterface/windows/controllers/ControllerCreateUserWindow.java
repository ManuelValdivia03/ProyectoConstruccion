package userinterface.windows.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.UserDAO;
import logic.logicclasses.User;
import userinterface.windows.CreateUserWindow;

public class ControllerCreateUserWindow implements EventHandler<ActionEvent> {
    private CreateUserWindow view;
    private UserDAO userDAO;

    public ControllerCreateUserWindow(CreateUserWindow view) {
        this.view = view;
        this.userDAO = new UserDAO();
        view.getAddButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getAddButton()) {
            handleAddUser();
        }
    }

    private void handleAddUser() {
        try {
            String name = view.getNameField().getText();
            String phone = view.getPhoneField().getText();
            char status = view.getStatusCombo().getValue().charAt(0);

            if (name.isEmpty() || phone.isEmpty()) {
                view.getResultLabel().setText("Nombre y teléfono son obligatorios");
                view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
                return;
            }

            User user = new User(0, name, phone, status);
            if (userDAO.addUser(user)) {
                view.getResultLabel().setText("Usuario agregado correctamente");
                view.getResultLabel().setStyle("-fx-text-fill: #009900;");
                clearFields();
            } else {
                view.getResultLabel().setText("Error al agregar usuario");
                view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
            }
        } catch (Exception e) {
            view.getResultLabel().setText("Error: " + e.getMessage());
            view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
        }
    }

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getStatusCombo().setValue("A - Activo");
    }
}
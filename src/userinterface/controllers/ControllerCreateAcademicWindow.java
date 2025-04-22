package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.AcademicDAO;
import logic.daos.UserDAO;
import logic.enums.AcademicType;
import logic.exceptions.RepeatedStaffNumber;
import logic.logicclasses.Academic;
import logic.logicclasses.User;
import userinterface.windows.CreateAcademicWindow;


import java.sql.SQLException;

public class ControllerCreateAcademicWindow implements EventHandler<ActionEvent> {
    private final CreateAcademicWindow view;
    private final AcademicDAO academicDAO;
    private final UserDAO userDAO;

    public ControllerCreateAcademicWindow(CreateAcademicWindow view) {
        this.view = view;
        this.academicDAO = new AcademicDAO();
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
            if (view.getNameField().getText().isEmpty() ||
                    view.getPhoneField().getText().isEmpty() ||
                    view.getStaffNumberField().getText().isEmpty()) {
                showError("Nombre, teléfono y número de personal son obligatorios");
                return;
            }

            User user = new User(0, view.getNameField().getText(),
                    view.getPhoneField().getText(), 'A');

            if (!userDAO.addUser(user)) {
                showError("Error al registrar el usuario");
                return;
            }

            Academic academic = new Academic(
                    user.getIdUser(),
                    user.getFullName(),
                    user.getCellPhone(),
                    'A',
                    view.getStaffNumberField().getText(),
                    AcademicType.valueOf(view.getTypeComboBox().getValue())
            );

            academicDAO.addAcademic(academic);
            showSuccess("Académico registrado correctamente");
            clearFields();

        } catch (RepeatedStaffNumber e) {
            showError("El número de personal ya está registrado");
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Tipo de académico inválido");
        }
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
    }

    private void showSuccess(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #009900;");
    }

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getStaffNumberField().clear();
    }
}
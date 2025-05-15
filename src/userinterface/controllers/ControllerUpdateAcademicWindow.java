package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.daos.AccountDAO;
import logic.daos.AcademicDAO;
import logic.daos.UserDAO;
import logic.enums.AcademicType;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.logicclasses.Academic;
import logic.logicclasses.Account;
import logic.logicclasses.User;
import userinterface.utilities.Validators;
import userinterface.windows.UpdateAcademicWindow;

import java.sql.SQLException;

public class ControllerUpdateAcademicWindow implements EventHandler<ActionEvent> {
    private final UpdateAcademicWindow view;
    private final AcademicDAO academicDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final Academic originalAcademic;
    private final String originalEmail;
    private final Stage currentStage;
    private final Runnable refreshCallback;

    public ControllerUpdateAcademicWindow(UpdateAcademicWindow view, Academic academic,
                                          String email, Stage stage, Runnable callback) {
        this.view = view;
        this.academicDAO = new AcademicDAO();
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        this.originalAcademic = academic;
        this.originalEmail = email;
        this.currentStage = stage;
        this.refreshCallback = callback;

        view.loadAcademicData(academic, email);
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> currentStage.close());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getUpdateButton()) {
            handleUpdateAcademic();
        }
    }

    private void handleUpdateAcademic() {
        try {
            clearError();

            if (!validateAllFields()) {
                return;
            }

            String name = view.getNameField().getText().trim();
            String phone = view.getPhoneField().getText().trim();
            String email = view.getEmailField().getText().trim();
            String password = view.getPassword();
            AcademicType type = AcademicType.valueOf(view.getTypeComboBox().getValue());
            char status = view.getStatusComboBox().getValue().charAt(0);

            verifyDataUniqueness(phone, email);

            updateUser(name, phone, status);
            updateAcademic(type);
            updateAccount(email, password);

            showSuccessAndClose();

        } catch (RepeatedCellPhoneException e) {
            showError("El número de teléfono ya está registrado");
            highlightField(view.getPhoneField());
        } catch (RepeatedEmailException e) {
            showError("El email ya está registrado");
            highlightField(view.getEmailField());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Tipo de académico inválido");
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();
        Validators validators = new Validators();

        if (view.getNameField().getText().trim().isEmpty()) {
            showError("Nombre completo es obligatorio");
            highlightField(view.getNameField());
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showError("Teléfono debe tener 10 dígitos");
            highlightField(view.getPhoneField());
            isValid = false;
        }

        if (view.getStaffNumberField().getText().trim().isEmpty()) {
            showError("Número de personal es obligatorio");
            highlightField(view.getStaffNumberField());
            isValid = false;
        }

        if (!validators.validateStaffNumber(view.getStaffNumberField().getText())) {
            showError("Número de personal debe tener 5 dígitos");
            highlightField(view.getStaffNumberField());
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            highlightField(view.getEmailField());
            isValid = false;
        }

        return isValid;
    }

    private void verifyDataUniqueness(String phone, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {
        if (!phone.equals(originalAcademic.getCellPhone())) {
            if (userDAO.cellPhoneExists(phone)) {
                throw new RepeatedCellPhoneException();
            }
        }

        if (!email.equals(originalEmail)) {
            if (accountDAO.accountExists(email)) {
                throw new RepeatedEmailException();
            }
        }
    }

    private void updateUser(String name, String phone, char status) throws SQLException {
        originalAcademic.setStatus(status);
        User user = new User(
                originalAcademic.getIdUser(),
                name,
                phone,
                originalAcademic.getStatus()
        );

        if (!userDAO.updateUser(user)) {
            throw new SQLException("No se pudo actualizar el usuario");
        }
    }

    private void updateAcademic(AcademicType type) throws SQLException {
        Academic academic = new Academic(
                originalAcademic.getIdUser(),
                view.getNameField().getText().trim(),
                view.getPhoneField().getText().trim(),
                originalAcademic.getStatus(),
                originalAcademic.getStaffNumber(),
                type
        );

        if (!academicDAO.updateAcademic(academic)) {
            throw new SQLException("No se pudo actualizar el académico");
        }
    }

    private void updateAccount(String email, String password) throws SQLException {
        Account account = new Account(
                originalAcademic.getIdUser(),
                email,
                password.isEmpty() ? null : password
        );

        if (!accountDAO.updateAccount(account)) {
            throw new SQLException("No se pudo actualizar la cuenta");
        }
    }

    private void showSuccessAndClose() {
        view.getResultLabel().setText("Académico actualizado correctamente");
        view.getResultLabel().setStyle("-fx-text-fill: #009900;");

        view.getUpdateButton().setDisable(true);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            currentStage.close();
                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }
                        });
                    }
                },
                2000
        );
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }
}
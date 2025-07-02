package userinterface.controllers;

import javafx.event.ActionEvent;
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
import logic.services.DataVerificationService;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.UpdateAcademicWindow;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;


public class ControllerUpdateAcademicWindow {
    private static final int SUCCESS_MESSAGE_DELAY_MS = 2000;
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String SUCCESS_COLOR = "-fx-text-fill: #009900;";
    private static final String ERROR_COLOR = "-fx-text-fill: #cc0000;";

    private final UpdateAcademicWindow view;
    private final AcademicDAO academicDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final Academic originalAcademic;
    private final String originalEmail;
    private final Stage currentStage;
    private final Runnable refreshCallback;
    private final Validators validators;

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
        this.validators = new Validators();

        initializeView();
        setupEventHandlers();
    }

    private void initializeView() {
        view.loadAcademicData(originalAcademic, originalEmail);
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this::handleUpdateAcademic);
        view.getCancelButton().setOnAction(event -> currentStage.close());
    }

    private void handleUpdateAcademic(ActionEvent event) {
        try {
            clearError();

            if (!validateAllFields()) {
                return;
            }

            AcademicFormData formData = getFormData();

            DataVerificationService.verifyAcademicUpdateUniqueness(
                formData.phone(), formData.email(), originalAcademic.getCellPhone(), originalEmail);

            updateAcademicFromFormData(formData);
            showSuccessAndClose();

        } catch (RepeatedCellPhoneException e) {
            showError("El número de teléfono ya está registrado", view.getPhoneField());
        } catch (RepeatedEmailException e) {
            showError("El email ya está registrado", view.getEmailField());
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        } catch (IllegalArgumentException e) {
            showError("Tipo de académico inválido");
        }
    }

    private AcademicFormData getFormData() {
        String name = view.getNameField().getText().trim();
        String phone = view.getPhoneField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String password = view.getPassword();
        AcademicType type = AcademicType.valueOf(view.getTypeComboBox().getValue());
        char status = view.getStatusComboBox().getValue().charAt(0);
        String staffNumber = view.getStaffNumberField().getText().trim();
        String extension = view.getPhoneExtensionField().getText().trim();
        return new AcademicFormData(name, phone, email, password, type, status, staffNumber, extension);
    }

    private void updateAcademicFromFormData(AcademicFormData data) throws SQLException {
        updateUser(data.name(), data.phone(), data.status());
        updateAcademic(data.type());
        updateAccount(data.email(), data.password());
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();

        isValid &= validateField(view.getNameField(),
                !view.getNameField().getText().trim().isEmpty(),
                "Nombre completo es obligatorio");

        isValid &= validateField(view.getPhoneField(),
                validators.validateCellPhone(view.getPhoneField().getText()),
                "Teléfono debe tener 10 dígitos");

        isValid &= validateField(view.getStaffNumberField(),
                validators.validateStaffNumber(view.getStaffNumberField().getText()),
                "Número de personal debe tener 5 dígitos");

        isValid &= validateField(view.getEmailField(),
                validators.validateEmail(view.getEmailField().getText()),
                "Formato de email inválido");

        String extension = view.getPhoneExtensionField().getText().trim();
        if (!extension.isEmpty() && !validators.validatePhoneExtension(extension)) {
            showError("Extensión debe ser numérica y máximo 5 dígitos", view.getPhoneExtensionField());
            isValid = false;
        }

        return isValid;
    }

    private boolean validateField(TextField field, boolean isValid, String errorMessage) {
        if (!isValid) {
            showError(errorMessage, field);
            return false;
        }
        return true;
    }

    private void updateUser(String name, String phone, char status) throws SQLException {
        User user = new User(
                originalAcademic.getIdUser(),
                name,
                phone,
                view.getPhoneExtensionField().getText().trim(),
                status
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
                view.getPhoneExtensionField().getText().trim(),
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
        view.getResultLabel().setStyle(SUCCESS_COLOR);
        view.getUpdateButton().setDisable(true);

        new Timer().schedule(
                new TimerTask() {
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
                SUCCESS_MESSAGE_DELAY_MS
        );
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getEmailField().setStyle("");
        view.getPhoneExtensionField().setStyle("");
    }

    private void showError(String message, TextField field) {
        showError(message);
        highlightField(field);
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle(ERROR_COLOR);
    }

    private void highlightField(TextField field) {
        field.setStyle(ERROR_STYLE);
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }

    private static record AcademicFormData(
            String name,
            String phone,
            String email,
            String password,
            AcademicType type,
            char status,
            String staffNumber,
            String extension
    ) {}
}

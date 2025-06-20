package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import logic.logicclasses.LinkedOrganization;

public class UpdateLinkedOrganizationWindow {
    private final VBox view;
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField emailField;
    private final ComboBox<String> statusComboBox;
    private final Button updateButton;
    private final Button cancelButton;
    private final Label resultLabel;
    private final TextField phoneExtensionField;
    private final TextField departmentField;

    public UpdateLinkedOrganizationWindow() {
        Label titleLabel = new Label("Actualizar Organización Vinculada");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();

        phoneExtensionField = new TextField();
        phoneExtensionField.setPromptText("Dejar vacío si no tiene");
        
        departmentField = new TextField();
        departmentField.setPromptText("Departamento o área");

        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("A", "I");
        statusComboBox.setValue("A");

        updateButton = new Button("Actualizar");
        updateButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Ext.:"), 2, 1);
        grid.add(phoneExtensionField, 3, 1);
        grid.add(new Label("Departamento:"), 0, 2);
        grid.add(departmentField, 1, 2, 3, 1);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3, 3, 1);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(statusComboBox, 1, 4);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, updateButton, cancelButton, resultLabel);
    }

    public void setOrganizationData(LinkedOrganization org) {
        nameField.setText(org.getNameLinkedOrganization());
        phoneField.setText(org.getCellPhoneLinkedOrganization());
        phoneExtensionField.setText(org.getPhoneExtension());
        departmentField.setText(org.getDepartment());
        emailField.setText(org.getEmailLinkedOrganization());
        statusComboBox.setValue(String.valueOf(org.getStatus()));
    }

    public LinkedOrganization getUpdatedOrganization(int id) {
        LinkedOrganization org = new LinkedOrganization();
        org.setIdLinkedOrganization(id);
        org.setNameLinkedOrganization(nameField.getText().trim());
        org.setCellPhoneLinkedOrganization(phoneField.getText().trim());
        org.setPhoneExtension(phoneExtensionField.getText().trim());
        org.setDepartment(departmentField.getText().trim());
        org.setEmailLinkedOrganization(emailField.getText().trim());
        org.setStatus(statusComboBox.getValue().charAt(0));
        return org;
    }

    public VBox getView() {
        return view;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getPhoneField() {
        return phoneField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public ComboBox<String> getStatusComboBox() {
        return statusComboBox;
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }

    public TextField getPhoneExtensionField() {
        return phoneExtensionField;
    }

    public TextField getDepartmentField() {
        return departmentField;
    }
}

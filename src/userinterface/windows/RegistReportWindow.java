package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import logic.enums.ReportType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegistReportWindow {
    private final VBox view;
    private final ComboBox<ReportType> typeComboBox;
    private final TextField hoursTextField;
    private final DatePicker datePicker;
    private final TextArea methodologyTextArea;
    private final TextArea descriptionTextArea;
    private final Button registerButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public RegistReportWindow() {
        Label titleLabel = new Label("Registrar Reporte");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(ReportType.values());
        typeComboBox.setPromptText("Seleccione el tipo");

        hoursTextField = new TextField();
        hoursTextField.setPromptText("Horas reportadas");

        datePicker = new DatePicker();
        datePicker.setPromptText("Seleccione fecha");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });

        methodologyTextArea = new TextArea();
        methodologyTextArea.setPromptText("Metodología utilizada");
        methodologyTextArea.setPrefRowCount(3);

        descriptionTextArea = new TextArea();
        descriptionTextArea.setPromptText("Descripción de actividades");
        descriptionTextArea.setPrefRowCount(5);

        registerButton = new Button("Registrar Reporte");
        registerButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Tipo de reporte:"), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Horas:"), 0, 1);
        grid.add(hoursTextField, 1, 1);
        grid.add(new Label("Fecha:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Metodología:"), 0, 3);
        grid.add(methodologyTextArea, 1, 3);
        grid.add(new Label("Descripción:"), 0, 4);
        grid.add(descriptionTextArea, 1, 4);

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, registerButton, cancelButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public ComboBox<ReportType> getTypeComboBox() {
        return typeComboBox;
    }

    public TextField getHoursTextField() {
        return hoursTextField;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public TextArea getMethodologyTextArea() {
        return methodologyTextArea;
    }

    public TextArea getDescriptionTextArea() {
        return descriptionTextArea;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }
}
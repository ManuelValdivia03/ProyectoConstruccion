package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.LinkedOrganization;

public class LinkOrganizationWindow {
    private final VBox view;
    private final TableView<LinkedOrganization> organizationTable;
    private final Button backButton;

    public LinkOrganizationWindow(EventHandler<ActionEvent> linkAction) {
        organizationTable = new TableView<>();
        organizationTable.setStyle("-fx-font-size: 14px;");
        organizationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LinkedOrganization, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nameLinkedOrganization"));
        nameCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LinkedOrganization, String> phoneCol = new TableColumn<>("Teléfono");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("cellPhoneLinkedOrganization"));
        phoneCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LinkedOrganization, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("emailLinkedOrganization"));
        emailCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LinkedOrganization, Void> linkCol = new TableColumn<>("Acción");
        linkCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Vincular");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    LinkedOrganization org = getTableView().getItems().get(getIndex());
                    if (org != null) {
                        btn.setUserData(org);
                        linkAction.handle(new ActionEvent(btn, null));
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        organizationTable.getColumns().addAll(nameCol, phoneCol, emailCol, linkCol);

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(new Label("Seleccione una organización para vincular:"), organizationTable, buttonBox);
    }

    public VBox getView() {
        return view;
    }

    public TableView<LinkedOrganization> getOrganizationTable() {
        return organizationTable;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setOrganizationData(ObservableList<LinkedOrganization> organizations) {
        organizationTable.setItems(organizations);
    }
}

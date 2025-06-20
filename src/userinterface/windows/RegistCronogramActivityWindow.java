package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import logic.logicclasses.Activity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public class RegistCronogramActivityWindow {
    private final BorderPane view;
    private final GridPane calendarGrid;
    private final Button previousMonth;
    private final Button nextMonth;
    private final Text monthYearText;
    private YearMonth currentYearMonth;
    private final Label statusLabel;
    private LocalDate selectedDate;
    private DayClickHandler dayClickHandler;

    public RegistCronogramActivityWindow() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setAlignment(Pos.CENTER);

        previousMonth = new Button("<");
        nextMonth = new Button(">");
        monthYearText = new Text();

        HBox navigationBar = new HBox(20);
        navigationBar.setAlignment(Pos.CENTER);
        navigationBar.getChildren().addAll(previousMonth, monthYearText, nextMonth);

        String[] dayNames = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < 7; i++) {
            Text dayLabel = new Text(dayNames[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            calendarGrid.add(dayLabel, i, 0);
        }

        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        view = new BorderPane();
        view.setTop(navigationBar);
        view.setCenter(calendarGrid);
        view.setBottom(statusLabel);
        view.setPadding(new Insets(15));

        view.setStyle("-fx-background-color: #f5f5f5;");
        monthYearText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        updateCalendar();
    }

    private void updateCalendar() {
        String mes = switch (currentYearMonth.getMonth()) {
            case JANUARY -> "Enero";
            case FEBRUARY -> "Febrero";
            case MARCH -> "Marzo";
            case APRIL -> "Abril";
            case MAY -> "Mayo";
            case JUNE -> "Junio";
            case JULY -> "Julio";
            case AUGUST -> "Agosto";
            case SEPTEMBER -> "Septiembre";
            case OCTOBER -> "Octubre";
            case NOVEMBER -> "Noviembre";
            case DECEMBER -> "Diciembre";
        };

        monthYearText.setText(mes + " " + currentYearMonth.getYear());

        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int firstDayOfMonth = currentYearMonth.atDay(1).getDayOfWeek().getValue() - 1;

        for (int i = 1; i <= daysInMonth; i++) {
            Button dayButton = new Button(String.valueOf(i));
            dayButton.setPrefWidth(40);
            dayButton.setPrefHeight(40);

            LocalDate date = currentYearMonth.atDay(i);
            if (date.equals(selectedDate)) {
                dayButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            }

            dayButton.setOnAction(event -> {
                if (dayClickHandler != null) {
                    dayClickHandler.onDayClicked(date);
                }
            });

            int row = (i + firstDayOfMonth - 1) / 7 + 1;
            int col = (i + firstDayOfMonth - 1) % 7;
            calendarGrid.add(dayButton, col, row);
        }
    }

    public void updateCalendarWithActivities(Map<LocalDate, Activity> activities) {
        String mes = switch (currentYearMonth.getMonth()) {
            case JANUARY -> "Enero";
            case FEBRUARY -> "Febrero";
            case MARCH -> "Marzo";
            case APRIL -> "Abril";
            case MAY -> "Mayo";
            case JUNE -> "Junio";
            case JULY -> "Julio";
            case AUGUST -> "Agosto";
            case SEPTEMBER -> "Septiembre";
            case OCTOBER -> "Octubre";
            case NOVEMBER -> "Noviembre";
            case DECEMBER -> "Diciembre";
        };

        monthYearText.setText(mes + " " + currentYearMonth.getYear());

        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int firstDayOfMonth = currentYearMonth.atDay(1).getDayOfWeek().getValue() - 1;

        for (int i = 1; i <= daysInMonth; i++) {
            Button dayButton = new Button(String.valueOf(i));
            dayButton.setPrefWidth(40);
            dayButton.setPrefHeight(40);

            LocalDate date = currentYearMonth.atDay(i);

            if (activities.containsKey(date)) {
                dayButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
            } else if (date.equals(selectedDate)) {
                dayButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            }

            final LocalDate finalDate = date;
            dayButton.setOnAction(event -> {
                if (dayClickHandler != null) {
                    selectedDate = finalDate;
                    dayClickHandler.onDayClicked(finalDate);
                }
            });

            int row = (i + firstDayOfMonth - 1) / 7 + 1;
            int col = (i + firstDayOfMonth - 1) % 7;
            calendarGrid.add(dayButton, col, row);
        }
    }

    public BorderPane getView() {
        return view;
    }

    public Button getPreviousMonthButton() {
        return previousMonth;
    }

    public Button getNextMonthButton() {
        return nextMonth;
    }

    public GridPane getCalendarGrid() {
        return calendarGrid;
    }

    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }

    public void setCurrentYearMonth(YearMonth yearMonth) {
        this.currentYearMonth = yearMonth;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        updateCalendar();
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setText(message);
    }

    public void setOnDayClicked(DayClickHandler handler) {
        dayClickHandler = handler;
    }

    public interface DayClickHandler {
        void onDayClicked(LocalDate date);
    }
}
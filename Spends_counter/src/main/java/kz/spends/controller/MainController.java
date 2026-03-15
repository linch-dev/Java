package kz.spends.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import kz.spends.model.Expense;
import kz.spends.utils.CSVHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;

public class MainController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextField amountField;
    @FXML private TextField descField;
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, String> dateCol;
    @FXML private TableColumn<Expense, String> catCol;
    @FXML private TableColumn<Expense, Double> amtCol;
    @FXML private TableColumn<Expense, String> descCol;
    @FXML private Label totalLabel;

    private ObservableList<Expense> allExpenses = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        categoryBox.setItems(FXCollections.observableArrayList(
                "Тамақ", "Көлік", "Білім", "Денсаулық",
                "Ойындар", "Киім", "Коммуналдық услугалар", "Басқа нәрселер"
        ));
        categoryBox.setValue("Тамақ");

        dateCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getDate()));
        catCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getCategory()));
        amtCol.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue().getAmount()));
        descCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getDescription()));

        expenseTable.setItems(allExpenses);

        allExpenses.addAll(CSVHandler.loadAll());
        updateTotal();
    }

    @FXML
    private void handleAdd() {
        if (datePicker.getValue() == null) {
            showAlert("Күнді таңдаңыз");
            return;
        }
        String date = datePicker.getValue().toString();
        String cat     = categoryBox.getValue();
        String amtText = amountField.getText().trim();
        String desc    = descField.getText().trim();

        if (date.isEmpty() || amtText.isEmpty() || desc.isEmpty()) {
            showAlert("Барлық бағандарды толтырыңыз");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Сумма тек сандардан болуы керек");
            return;
        }

        Expense expense = new Expense(date, cat, amount, desc);
        allExpenses.add(expense);

        CSVHandler.saveAll(new ArrayList<>(allExpenses));
        updateTotal();

        datePicker.setValue(null);
        amountField.clear();
        descField.clear();
    }

    @FXML
    private void handleDelete() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Жойылатын жолды таңда");
            return;
        }
        allExpenses.remove(selected);
        CSVHandler.saveAll(new ArrayList<>(allExpenses));
        updateTotal();
    }

    @FXML
    private void handleShowAll() {
        allExpenses.setAll(CSVHandler.loadAll());
        updateTotal();
    }

    @FXML
    private void handleFilter() {
        String selectedCat = categoryBox.getValue();
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : CSVHandler.loadAll()) {
            if (e.getCategory().equals(selectedCat)) {
                filtered.add(e);
            }
        }
        allExpenses.setAll(filtered);
        updateTotal();
    }

    @FXML
    private void handleStats() {

        Map<String, Double> catTotals = new TreeMap<>();
        for (Expense e : allExpenses) {
            catTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
        }

        if (catTotals.isEmpty()) {
            showAlert("Статистика үшін деректер жоқ");
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
            pieData.add(new PieChart.Data(
                    entry.getKey() + " (" + String.format("%.0f", entry.getValue()) + "₸)",
                    entry.getValue()
            ));
        }

        PieChart chart = new PieChart(pieData);
        chart.setTitle("Тип бойынша шығындар");

        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Статистика");
        javafx.scene.Scene scene = new javafx.scene.Scene(
                new javafx.scene.layout.VBox(chart), 500, 420
        );
        stage.setScene(scene);
        stage.show();
    }

    private void updateTotal() {
        double total = 0;
        for (Expense e : allExpenses) {
            total += e.getAmount();
        }
        totalLabel.setText(String.format("Жалпы: %.0f тг", total));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
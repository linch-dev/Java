package kz.spends.utils;

import kz.spends.model.Expense;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CSVHandler {

    private static final String FILE_NAME = "expenses.csv";

    public static List<Expense> loadAll() {
        List<Expense> list = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",", 4);

                if (parts.length == 4) {
                    String date        = parts[0];
                    String category    = parts[1];
                    double amount      = Double.parseDouble(parts[2]);
                    String description = parts[3];
                    list.add(new Expense(date, category, amount, description));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
        }

        return list;
    }

    public static void saveAll(List<Expense> expenses) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME), "UTF-8"))) {
            for (Expense e : expenses) {
                writer.write(e.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи: " + e.getMessage());
        }
    }
}
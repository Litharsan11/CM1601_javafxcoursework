package com.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class App extends Application {

    public ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Government Tax Department System");

        TextField filePathField = new TextField();
        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("CSV", "TSV", "JSON", "XML");
        formatCombo.setValue("CSV");
        Button importButton = new Button("Import File");
        Label statusLabel = new Label();
        Label summaryLabel = new Label();
        TableView<Transaction> table = new TableView<>();
        table.setItems(transactions);

        table.getColumns().addAll(
                createCol("Bill No", "billNumber"),
                createCol("Item Code", "itemCode"),
                createCol("Sale Price", "salePrice"),
                createCol("Quantity", "quantity"),
                createCol("Line Total", "lineTotal"),
                createCol("Discount", "discount"),
                createCol("Internal Price", "internalPrice"),
                createCol("Checksum", "checksum"),
                createCol("Valid", "isValid"),
                createCol("Profit", "profit"));

        Button deleteInvalidBtn = new Button("Delete Invalid");
        deleteInvalidBtn.setOnAction(e -> {
            transactions.removeIf(t -> !t.getIsValid());
            updateSummary(summaryLabel);
        });

        Button deleteZeroProfitBtn = new Button("Delete Zero-Profit");
        deleteZeroProfitBtn.setOnAction(e -> {
            transactions.removeIf(t -> t.getProfit() == 0);
            updateSummary(summaryLabel);
        });

        HBox buttonRow = new HBox(10, importButton, deleteInvalidBtn, deleteZeroProfitBtn);

        importButton.setOnAction(e -> {
            String path = filePathField.getText().trim();
            if (path.isEmpty()) {
                statusLabel.setText("Enter a file path");
                return;
            }
            File file = new File(path);
            if (!file.exists()) {
                statusLabel.setText("File does not exist");
                return;
            }
            try {
                transactions.clear();
                String format = formatCombo.getValue();
//                switch (format) {
//                    case "CSV":
//                        importDelimited(file, ",");
//                    case "TSV":
//                        importDelimited(file, "\t");
//                    case "JSON":
//                        importJSON(file);
//                    case "XML":
//                        importXML(file);
//                }
                switch (format) {
                    case "CSV":
                        importDelimited(file, ",");
                        break;
                    case "TSV":
                        importDelimited(file, "\t");
                        break;
                    case "JSON":
                        importJSON(file);
                        break;
                    case "XML":
                        importXML(file);
                        break;
                }
                updateSummary(summaryLabel);
                statusLabel.setText("Imported successfully.");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox root = new VBox(10, filePathField, formatCombo, buttonRow, statusLabel, summaryLabel, table);
        root.setPadding(new Insets(10));
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();
    }

    private <T> TableColumn<Transaction, T> createCol(String title, String prop) {
        TableColumn<Transaction, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    public void importDelimited(File file, String delimiter) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(delimiter);
                if (parts.length == 8)
                    addTransaction(parts);
            }
        }
    }

    private void importJSON(File file) throws IOException, JSONException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                json.append(line);
        }
        JSONArray array = new JSONArray(json.toString());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            transactions.add(new Transaction(
                    obj.getString("bill_number"),
                    obj.getString("item_code"),
                    obj.getDouble("sale_price"),
                    obj.getInt("quantity"),
                    obj.getDouble("line_total"),
                    obj.getDouble("discount"),
                    obj.getDouble("internal_price"),
                    obj.getString("checksum")));
        }
    }

    private void importXML(File file) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(file);
        NodeList nodes = doc.getElementsByTagName("Transaction");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            transactions.add(new Transaction(
                    e.getElementsByTagName("bill_number").item(0).getTextContent(),
                    e.getElementsByTagName("item_code").item(0).getTextContent(),
                    Double.parseDouble(e.getElementsByTagName("sale_price").item(0).getTextContent()),
                    Integer.parseInt(e.getElementsByTagName("quantity").item(0).getTextContent()),
                    Double.parseDouble(e.getElementsByTagName("line_total").item(0).getTextContent()),
                    Double.parseDouble(e.getElementsByTagName("discount").item(0).getTextContent()),
                    Double.parseDouble(e.getElementsByTagName("internal_price").item(0).getTextContent()),
                    e.getElementsByTagName("checksum").item(0).getTextContent()));
        }
    }

    public void addTransaction(String[] parts) {
        try {
            transactions.add(new Transaction(
                    parts[0], parts[1],
                    Double.parseDouble(parts[2]),
                    Integer.parseInt(parts[3]),
                    Double.parseDouble(parts[4]),
                    Double.parseDouble(parts[5]),
                    Double.parseDouble(parts[6]),
                    parts[7]));
        } catch (Exception ignored) {
        }
    }

    private void updateSummary(Label summaryLabel) {
        long valid = transactions.stream().filter(Transaction::getIsValid).count();
        summaryLabel.setText(
                "Total: " + transactions.size() + ", Valid: " + valid + ", Invalid: " + (transactions.size() - valid));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

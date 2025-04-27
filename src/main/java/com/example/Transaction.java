package com.example;

import java.util.regex.Pattern;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Transaction {
    private String billNumber;
    private String itemCode;
    private double salePrice;
    private int quantity;
    private double lineTotal;
    private double discount;
    private double internalPrice;
    private String checksum;
    private boolean isValid;
    private double profit;

    public Transaction(String billNumber, String itemCode, double salePrice, int quantity,
            double lineTotal, double discount, double internalPrice, String checksum) {
        this.billNumber = billNumber;
        this.itemCode = itemCode;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.discount = discount;
        this.internalPrice = internalPrice;
        this.checksum = checksum;
        this.isValid = validateChecksum();
        this.profit = calculateProfit();
    }

    public boolean validateChecksum() {
        String calculatedChecksum = calculateChecksum();
        return calculatedChecksum.equals(this.checksum) && !hasSpecialChars() && internalPrice >= 0;
    }

    public String calculateChecksum() {
        String line = itemCode + internalPrice + discount + salePrice + quantity;
        int upper = 0, lower = 0, digit = 0;
        for (char c : line.toCharArray()) {
            if (Character.isUpperCase(c))
                upper++;
            else if (Character.isLowerCase(c))
                lower++;
            else if (Character.isDigit(c) || c == '.')
                digit++;
        }
        return String.valueOf(upper + lower + digit);
    }

    public boolean hasSpecialChars() {
        return Pattern.compile("[^a-zA-Z0-9_]").matcher(itemCode).find();
    }

    public double calculateProfit() {
        return (internalPrice * quantity) - ((salePrice * quantity) - discount);
    }

    // Getters and Setters
    public String getBillNumber() {
        return billNumber;
    }

    public String getItemCode() {
        return itemCode;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public double getInternalPrice() {
        return internalPrice;
    }

    public String getChecksum() {
        return checksum;
    }

    public boolean getIsValid() {
        return isValid;
    }

    public double getProfit() {
        return profit;
    }
}

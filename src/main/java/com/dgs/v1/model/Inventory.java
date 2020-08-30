package com.dgs.v1.model;

import com.dgs.v1.util.DbUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class Inventory {
    public static final String amount = "amount";
    private List<HashMap<String, Object>> stocks;
    private List<HashMap<String, Object>> reservedForClient;
    private List<HashMap<String, Object>> purchasedFromSupplier;
    private final Double currentStockLevel; //(sum of a,b,c)
    private final Double purchasedYetReceived; //(PYR)
    private final Double reservedYetPicked; //(RYR)

    public Inventory(List<HashMap<String, Object>> stocks, List<HashMap<String, Object>> reservedForClient, List<HashMap<String, Object>> purchasedFromSupplier) {
        for(HashMap<String, Object> row : stocks){
            row.put("type", "stock");
        }
        this.stocks = stocks;
        for(HashMap<String, Object> row : reservedForClient){
            row.put("type", "reservedForClient");
        }
        this.reservedForClient = reservedForClient;
        for(HashMap<String, Object> row : purchasedFromSupplier){
            row.put("type", "purchasedYetReceived");
        }
        this.purchasedFromSupplier = purchasedFromSupplier;

        this.currentStockLevel = getTotalStock();
        this.purchasedYetReceived = getTotalPurchasedFromSupplier();
        this.reservedYetPicked = getTotalReservedForClient();
    }

    //Get stock number based on house A, B or Cs
    public Double getStockFromHouse(String house) {
        Double stock = 0.0;
        for(HashMap<String, Object> row : stocks){
            if(house.equals(row.get("shouseno"))) {
                stock =  DbUtils.getDouble(row.get(amount));
            }
        }
        return stock;
    }

    //Sum all stocks together
    public Double getTotalStock() {
        Double sum = 0.0;
        for(HashMap<String, Object> row : stocks){
            sum +=  DbUtils.getDouble(row.get(amount));
        }
        return sum;
    }

    public Double getTotalReservedForClient() {
        Double sum = 0.0;
        for(HashMap<String, Object> row : reservedForClient){
            sum +=  DbUtils.getDouble(row.get(amount));
        }
        return sum;
    }

    public Double getTotalPurchasedFromSupplier() {
        Double sum = 0.0;
        for(HashMap<String, Object> row : purchasedFromSupplier){
            sum +=  DbUtils.getDouble(row.get(amount));
        }
        return sum;
    }

    public Double getCurrentStockLevel() {
        return currentStockLevel;
    }

    public Double getPurchasedYetReceived() {
        return purchasedYetReceived;
    }

    public Double getReservedYetPicked() {
        return reservedYetPicked;
    }

    public List<HashMap<String, Object>> getStocks() {
        return stocks;
    }

    public List<HashMap<String, Object>> getReservedForClient() {
        return reservedForClient;
    }

    public List<HashMap<String, Object>> getPurchasedFromSupplier() {
        return purchasedFromSupplier;
    }
}

package com.dgs.v1.model;

import com.dgs.v1.service.Context;

public class ProductDemand extends Entity {

    private String date;

    private Double monthlydemand;

    @Override
    public Delegate getDelegate(Context ctx) {
        return new ProductDelegate(ctx);
    }

    public ProductDemand() {
        super();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getMonthlydemand() {
        return monthlydemand;
    }

    public void setMonthlydemand(Double monthlydemand) {
        this.monthlydemand = monthlydemand;
    }

    @Override
    public String toString() {
        return "ProductDemand{" +
                "date='" + date + '\'' +
                ", monthlydemand=" + monthlydemand +
                '}';
    }

}
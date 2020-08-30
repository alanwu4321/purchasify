package com.dgs.v1.model;

public class PurchaseStrategy {
    private final double idealPurchaseQty;
    private final double actualPurchaseQty;
    private final double newServiceLvl;
    private final double newServiceCoefficient;
    private final double safetyStock;
    private final double b_inspectionLevel;
    private final double b_recommendDemand;
    private final double b_idealPurchaseQtyMonth;
    private final double b_actualPurchaseQtyMonth;
    private final double b_safetyStock;
    private final double b_adjustedActualPurchaseQty;
    private final double b_adjustedMaxStockLvl;
    private final double b_adjustedMaxStockLvlMonth;
    private final double b_adjustedActualPurchaseQtyMonth;
    private final double b_diffBetweenAdjustment;

    public PurchaseStrategy(double idealPurchaseQty, double actualPurchaseQty,  double newServiceLvl, double newServiceCoefficient,
            double safetyStock, double b_inspectionLevel, double b_recommendDemand, double b_idealPurchaseQtyMonth,
            double b_actualPurchaseQtyMonth, double b_safetyStock, double b_adjustedActualPurchaseQty,
            double b_adjustedMaxStockLvl, double b_adjustedMaxStockLvlMonth, double b_adjustedActualPurchaseQtyMonth,
            double b_diffBetweenAdjustment) {
        this.idealPurchaseQty = idealPurchaseQty;
        this.actualPurchaseQty = actualPurchaseQty;
        this.newServiceLvl = newServiceLvl;
        this.newServiceCoefficient = newServiceCoefficient;
        this.safetyStock = safetyStock;
        this.b_inspectionLevel = b_inspectionLevel;
        this.b_recommendDemand = b_recommendDemand;
        this.b_idealPurchaseQtyMonth = b_idealPurchaseQtyMonth;
        this.b_actualPurchaseQtyMonth = b_actualPurchaseQtyMonth;
        this.b_safetyStock = b_safetyStock;
        this.b_adjustedActualPurchaseQty = b_adjustedActualPurchaseQty;
        this.b_adjustedMaxStockLvl = b_adjustedMaxStockLvl;
        this.b_adjustedMaxStockLvlMonth = b_adjustedMaxStockLvlMonth;
        this.b_adjustedActualPurchaseQtyMonth = b_adjustedActualPurchaseQtyMonth;
        this.b_diffBetweenAdjustment = b_diffBetweenAdjustment;
    }

    public double getNewServiceLvl() {
        return newServiceLvl;
    }

    public double getIdealPurchaseQty() {
        return idealPurchaseQty;
    }

    public double getActualPurchaseQty() {
        return actualPurchaseQty;
    }

    public double getNewServiceCoefficient() {
        return newServiceCoefficient;
    }

    public double getSafetyStock() {
        return safetyStock;
    }

    public double getB_inspectionLevel() {
        return b_inspectionLevel;
    }

    public double getB_recommendDemand() {
        return b_recommendDemand;
    }

    public double getB_idealPurchaseQtyMonth() {
        return b_idealPurchaseQtyMonth;
    }

    public double getB_adjustedActualPurchaseQty() {
        return b_adjustedActualPurchaseQty;
    }

    public double getB_actualPurchaseQtyMonth() {
        return b_actualPurchaseQtyMonth;
    }

    public double getB_adjustedMaxStockLvlMonth() {
        return b_adjustedMaxStockLvlMonth;
    }

    public double getB_safetyStock() {
        return b_safetyStock;
    }

    public double getB_adjustedMaxStockLvl() {
        return b_adjustedMaxStockLvl;
    }

    public double getB_adjustedActualPurchaseQtyMonth() {
        return b_adjustedActualPurchaseQtyMonth;
    }

    public double getB_diffBetweenAdjustment() {
        return b_diffBetweenAdjustment;
    }
}

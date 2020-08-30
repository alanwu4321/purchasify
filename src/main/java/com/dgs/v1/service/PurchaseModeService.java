package com.dgs.v1.service;

import com.dgs.v1.model.ExponentialSmoothingModel;
import com.dgs.v1.model.Inventory;
import com.dgs.v1.model.PurchaseStrategy;
import com.dgs.v1.model.ServiceLevelChart;
import com.dgs.v1.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

//import com.dgs.v1.model.PurchaseStrategy;

//TODO CALC EVERYTHING BEYOND MODEL AND INV
@Service
public class PurchaseModeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseModeService.class);

    private ExponentialSmoothingModel model;
    private Inventory inventory;

    public PurchaseStrategy calculate(Context ctx, ExponentialSmoothingModel model, Inventory inventory){
        // sanitize forecast and error ("nan" value will cause calculation to throw "nan")
        if (Double.isNaN(model.getForecast())) {
            model.setForecast(0);
        }
        if (Double.isNaN(model.getError())) {
            model.setError(0);
        }

        //一年可接受庫存不足總額
        double qtyAcceptableInYear = MathUtils.Sum(model.getAdjustedDemands()) * (1 - ctx.getServiceLevel()/100) ;

        //前置期用量(月)
        double qtyDuringLeadTime = model.getForecast() * ctx.getLeadTime();

        //檢視期用量(月)
//        double qtyDuringOrder = model.getForecast() * ctx.getOrderFrequencyInMonth();
//        LOGGER.info("檢視期用量(月)" + qtyDuringOrder);

        //每次偏差(不足總額/頻率)
        double errorEachTime = Math.min(qtyAcceptableInYear/ctx.getOrderFrequencyPerYear(), qtyDuringLeadTime);

        double _newServiceLvl = (1 - (errorEachTime/qtyDuringLeadTime)) * 100;

        //find the closest match
        double _newServiceCoefficient = ServiceLevelChart.get(_newServiceLvl);

        /*
            依訂購頻率 (F)
         */

        //安全庫存
        ctx.setOrderFrequencyInMonth(12/ctx.getOrderFrequencyPerYear());

        double _safetyStock = Math.sqrt(ctx.getLeadTime() + ctx.getOrderFrequencyInMonth()) * _newServiceCoefficient * model.getError();
        //目標庫存水準 aka max stockLvl
        double maxStockLvl = (model.getForecast() * (ctx.getLeadTime() + ctx.getOrderFrequencyInMonth())) + _safetyStock;

        double avgStockLvl = (maxStockLvl + _safetyStock)/2;

//      週轉率(次)
        double turnOverRate = (model.getForecast() * 12 ) / avgStockLvl;
        if(Double.isInfinite(turnOverRate)) turnOverRate = 0;

        //目前庫存持有(週)
        double currentStock = (inventory.getTotalStock() * 52)/ MathUtils.Sum(model.getDemands()) ;
        if(Double.isInfinite(currentStock)) currentStock = 0;

        //建議訂購量
        double _idealPurchaseQty = maxStockLvl - inventory.getTotalStock();

        //實際需購量
        double _actualPurchaseQty = _idealPurchaseQty - inventory.getTotalPurchasedFromSupplier() + inventory.getTotalReservedForClient();

        /*
            Part B
         */

        //檢視水準
        double b_inspectionLevel = _safetyStock + model.getForecast() * ctx.getLeadTime();

        //建議需求量 / 目標庫存水準
        double b_recommendDemand = _idealPurchaseQty + inventory.getTotalStock();

        //建議需求量/月
        double b_idealPurchaseQtyMonth = b_recommendDemand/model.getForecast();
        if(Double.isInfinite(b_idealPurchaseQtyMonth)) b_idealPurchaseQtyMonth = 0;

        //實際需購量(月)
        double b_actualPurchaseQtyMonth = _actualPurchaseQty/model.getForecast();
        if(Double.isInfinite(b_actualPurchaseQtyMonth)) b_actualPurchaseQtyMonth = 0;

        //安全庫存 (月)
        double b_safetyStock = _safetyStock/model.getForecast();
        if(Double.isInfinite(b_safetyStock)) b_safetyStock = 0;

        //調整後需購量 ??
        double b_adjustedActualPurchaseQty = Math.round(_actualPurchaseQty);

        //調整後目標庫存水準
        double b_adjustedMaxStockLvl = inventory.getTotalStock() + b_adjustedActualPurchaseQty + inventory.getTotalPurchasedFromSupplier() - inventory.getTotalReservedForClient();

        //調整後目標庫存水準(月)
        double b_adjustedMaxStockLvlMonth = b_adjustedMaxStockLvl/model.getForecast();
        if(Double.isInfinite(b_adjustedMaxStockLvlMonth)) b_adjustedMaxStockLvlMonth = 0;

        //調整後需購量(月)
        double b_adjustedActualPurchaseQtyMonth = b_adjustedActualPurchaseQty/model.getForecast();
        if(Double.isInfinite(b_adjustedActualPurchaseQtyMonth)) b_adjustedActualPurchaseQtyMonth = 0;


        //調整前後差異
        double b_diffBetweenAdjustment = b_adjustedActualPurchaseQty - b_actualPurchaseQtyMonth;

        return new PurchaseStrategy(
                _idealPurchaseQty,
                _actualPurchaseQty,
                _newServiceLvl,
                _newServiceCoefficient,
                _safetyStock,
                b_inspectionLevel,
                b_recommendDemand,
                b_idealPurchaseQtyMonth,
                b_actualPurchaseQtyMonth,
                b_safetyStock,
                b_adjustedActualPurchaseQty,
                b_adjustedMaxStockLvl,
                b_adjustedMaxStockLvlMonth,
                b_adjustedActualPurchaseQtyMonth,
                b_diffBetweenAdjustment
        );
    }


}

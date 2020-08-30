package com.dgs.v1.service;

import com.dgs.v1.util.MathUtils;
import com.dgs.v1.model.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.CompletableFuture.supplyAsync;

//TODO struc for the payload per product (ResponseBodies will be joined together in the controller)
//TODO RENAME TO FORECAST SERVICE
@Service
@EnableAsync
public class ResponseBodyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseBodyService.class);
    @Autowired
    ForecastService forecastService;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    PurchaseModeService purchaseModeService;
    @Qualifier("taskExecutor")
    @Autowired
    private Executor some;

    // Function return true if given element
    // found in array
    private static boolean check(String[] arr, String toCheckValue) {
        for (String element : arr) {
            if (element.equals(toCheckValue)) {
                return true;
            }
        }
        return false;
    }

    public static CompletableFuture<Void> runAsyncOrTimeout(
            Runnable runnable, long timeout, TimeUnit unit) {

        CompletableFuture<Void> other = new CompletableFuture<>();
        Executors.newScheduledThreadPool(
                1,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("timeoutafter-%d")
                        .build())
                .schedule(() -> {
                    TimeoutException ex = new TimeoutException(
                            "Timeout after " + timeout);
                    return other.completeExceptionally(ex);
                }, timeout, unit);
        return CompletableFuture.runAsync(runnable).applyToEither(other, a -> a);
    }

    //{"貨品編號", "貨品名稱", "採購類型", "A倉存量", "B倉存量", "C倉存量", "目前存量", "建議訂購量", "已採未交(-)", "已訂未交(-)", "實際需購量", "預估月需求 ", "安全庫存 ", "檢視水準", "建議需求量", "建議需求量/月", "實際需購量(月)", "安全庫存 (月)", "調整後需購量", "調整後目標庫存水準", "調整後目標庫存水準(月)", "調整後需購量(月)", "調整前後差異"};
    public void writeExcel(Context ctx, ExponentialSmoothingModel model, Product product, Inventory inventory, PurchaseStrategy purchaseStrategy) {
        try {
            ctx.getMutex().acquire();
            try {
                //++var is the value of the variable after the increment is applied.
                Integer _row = ctx.getRow();
                Row row = ctx.getSheet().createRow(_row);
                ctx.getColumns();
                // 貨品編號
                row.createCell(0).setCellValue(product.getSstkno());
                // 貨品名稱
                row.createCell(1).setCellValue(product.getSstkname());
                // 採購類型
                row.createCell(2).setCellValue(product.getSstkcolor());
                // A倉存量
                Cell cell = row.createCell(3);
                if (check(ctx.getWarehouseFilter(), "A"))
                    cell.setCellValue(inventory.getStockFromHouse("A"));
                // B倉存量
                cell = row.createCell(4);
                if (check(ctx.getWarehouseFilter(), "B"))
                    cell.setCellValue(inventory.getStockFromHouse("B"));
                // C倉存量
                cell = row.createCell(5);
                if (check(ctx.getWarehouseFilter(), "C"))
                    cell.setCellValue(inventory.getStockFromHouse("C"));
                // 目前存量
                row.createCell(6).setCellValue(MathUtils.round(inventory.getTotalStock()));
                // 建議訂購量
                row.createCell(7).setCellValue(MathUtils.round(purchaseStrategy.getIdealPurchaseQty()));
                // 已採未交
                row.createCell(8).setCellValue(MathUtils.round(inventory.getTotalPurchasedFromSupplier()));
                // 已訂未交
                row.createCell(9).setCellValue(MathUtils.round(inventory.getTotalReservedForClient()));
                // 實際需購量
                row.createCell(10).setCellValue(MathUtils.round(purchaseStrategy.getActualPurchaseQty()));
                //預估月需求
                row.createCell(11).setCellValue(MathUtils.round(model.getForecast()));
                // 安全庫存 => Round to Integer
                row.createCell(12).setCellValue(Math.round(purchaseStrategy.getSafetyStock()));

                //檢視水準
                row.createCell(13).setCellValue(MathUtils.round(purchaseStrategy.getB_inspectionLevel()));

                //建議需求量
                row.createCell(14).setCellValue(MathUtils.round(purchaseStrategy.getB_recommendDemand()));

                //建議需求量/月
                row.createCell(15).setCellValue(MathUtils.round(purchaseStrategy.getB_idealPurchaseQtyMonth()));

                //實際需購量(月)
                row.createCell(16).setCellValue(MathUtils.round(purchaseStrategy.getB_actualPurchaseQtyMonth()));

                //安全庫存 (月)
                row.createCell(17).setCellValue(MathUtils.round(purchaseStrategy.getB_safetyStock()));

                //調整後需購量 ??
                row.createCell(18).setCellValue(MathUtils.round(purchaseStrategy.getB_adjustedActualPurchaseQty()));

                //調整後目標庫存水準
                row.createCell(19).setCellValue(MathUtils.round(purchaseStrategy.getB_adjustedMaxStockLvl()));

                //調整後目標庫存水準(月)
                row.createCell(20).setCellValue(MathUtils.round(purchaseStrategy.getB_adjustedMaxStockLvlMonth()));

                //調整後需購量(月)
                row.createCell(21).setCellValue(MathUtils.round(purchaseStrategy.getB_adjustedActualPurchaseQtyMonth()));

                //調整前後差異
                row.createCell(22).setCellValue(MathUtils.round(purchaseStrategy.getB_diffBetweenAdjustment()));

                //New coefficient
                row.createCell(23).setCellValue(MathUtils.round(purchaseStrategy.getNewServiceCoefficient()));

                //New level
                row.createCell(24).setCellValue(MathUtils.round(purchaseStrategy.getNewServiceLvl()));

                ctx.setRow(++_row);
            } finally {
                ctx.getMutex().release();
            }
        } catch (Exception ie) {
            LOGGER.error(ie.getMessage());
        }

    }

    public ResponseBody getResponseBodyForProduct(Context ctx, Product product) {
        ExponentialSmoothingModel model = forecastService.getMonthlyDemandForecastByProduct(ctx, product);
        Inventory inventory = inventoryService.getInventoryByProduct(ctx, product);
        PurchaseStrategy purchaseStrategy = purchaseModeService.calculate(ctx, model, inventory);

        return new ResponseBody(ctx, product, model, inventory, purchaseStrategy);
    }

    public ResponseBody getResponseBodyForProductExcel(Context ctx, Product product, Boolean isExcel) {
        LOGGER.info(String.format("[ %s ] Async start ", product.getSstkno() ));
        final long start = System.currentTimeMillis();
        ExponentialSmoothingModel model = forecastService.getMonthlyDemandForecastByProduct(ctx, product);
        Inventory inventory = inventoryService.getInventoryByProduct(ctx, product);
        PurchaseStrategy purchaseStrategy = purchaseModeService.calculate(ctx, model, inventory);
        if(isExcel)
            writeExcel(ctx, model, product, inventory, purchaseStrategy);
        LOGGER.info(String.format("[ %s ] Elapsed time: %s ", product.getSstkno(), (System.currentTimeMillis() - start)));
        return new ResponseBody(ctx, product, model, inventory, purchaseStrategy);
    }


    @Async
    public CompletableFuture<ResponseBody> getResponseBodyForProductThread(Context ctx, Product product, ThreadPoolTaskExecutor executor, Boolean isExcel) {
        return supplyAsync(() -> {
            return getResponseBodyForProductExcel(ctx, product, isExcel);
        }, executor).orTimeout( 90, TimeUnit.SECONDS).exceptionally(
                e -> {
                        LOGGER.error(String.format("[ %s ] Error %s ",  product.getSstkno(),  e.getMessage()));
                    return null;
                }
        );
    }


    public List<HashMap<String, Object>> getResponseBodyForAllProducts(Context ctx) {
        return null;
    }
}

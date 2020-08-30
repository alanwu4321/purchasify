package com.dgs.v1.model;

import com.dgs.v1.service.Context;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ResponseBody{

    private final String key;

    private final Context ctx;

    //Product
    private final Product product;

    //PurchaseStrategy
    private final PurchaseStrategy purchaseStrategy;

    //Inventory
    private final Inventory inventory;


    private final ExponentialSmoothingModel model;

    public ResponseBody(Context ctx,
                        Product product,
                        ExponentialSmoothingModel model,
                        Inventory inventory,
                        PurchaseStrategy purchaseStrategy) {
        //key
        this.key = product.getSstkno();

        //Context
        this.ctx = ctx;

        //Product
        this.product = product;

        //PurchaseStrategy
        this.purchaseStrategy = purchaseStrategy;

        //Model
        this.model = model;

        //Model
        this.inventory = inventory;

    }

    public String getKey() {
        return key;
    }

    public Context getCtx() {
        return ctx;
    }

    public Product getProduct() {
        return product;
    }

    public PurchaseStrategy getPurchaseStrategy() {
        return purchaseStrategy;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ExponentialSmoothingModel getModel() {
        return model;
    }
}

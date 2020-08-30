package com.dgs.v1.service;

import com.dgs.v1.model.Inventory;
import com.dgs.v1.model.InventoryDelegate;
import com.dgs.v1.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

    public Inventory getInventoryByProduct(Context ctx, Product product) {
        InventoryDelegate inventoryDelegate = ctx.getInventoryDelegate();
        return new Inventory(
                inventoryDelegate.queryTotalStock(product.getSstkno()),
                inventoryDelegate.queryOrderYetPickedByPid(product.getSstkno()),
                inventoryDelegate.queryPurchasedYetReceivedByPid(product.getSstkno())
        );
    }
}

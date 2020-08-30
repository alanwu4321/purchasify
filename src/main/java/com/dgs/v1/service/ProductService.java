package com.dgs.v1.service;

import com.dgs.v1.model.Product;
import com.dgs.v1.model.ProductDelegate;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

@Service
public class ProductService extends AbstractService {
    protected ProductService() {
        super(Product.class);
    }

    public List<HashMap<String, Object>> getAllProducts(Context ctx) {
        ProductDelegate productDelegate = ctx.getProductDelegate();
        List<HashMap<String, Object>> productMap = productDelegate.getAllProducts();
        return productMap;
    }

    public Product getProductByPid(Context ctx, String pid) {
        ProductDelegate productDelegate = ctx.getProductDelegate();
        List<HashMap<String, Object>> productMap = productDelegate.getProductInfoByProductID(pid);
        return Product.buildProductFromMap(productMap);
    }
}

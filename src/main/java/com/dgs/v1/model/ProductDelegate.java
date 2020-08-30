package com.dgs.v1.model;


// Tbsstock 貨品基本資料表

import com.dgs.v1.service.Context;
import com.dgs.v1.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public class ProductDelegate extends Delegate<Product> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDelegate.class);
    private final Context ctx;

    public ProductDelegate(Context ctx){
        super(Product.class);
        this.ctx = ctx;
    }

    //and sstkno in ('A', 'B')
    private String getStringArray(String [] selected) {
        StringBuilder str = new StringBuilder();
        String prefix = "";
        for (String w : selected) {
            str.append(prefix);
            prefix = ",";
            str.append('\'' + w + "'");
        }
        return str.toString();
    }


    @Override
    public List<Product> getByPrefix(String prefix, Integer limit) {
        String sql = String.format("\n" +
                "SELECT sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tFROM tbsstock\n" +
                "\tWHERE sstkno \n" +
                "\tLIKE '%s%%' ORDER BY sstkno ASC LIMIT %s", prefix, limit);

        List<HashMap<String, Object>> products = DbUtils.query(ctx, sql);
        return this.getFromList(products);
    }

    @Override
    public List<Product> getBySelected(String[] selected, Integer limit) {
        String sql = String.format("\n" +
                "SELECT sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tFROM tbsstock\n" +
                "\tWHERE sstkno \n" +
                "\tin (%s)\n" +
                "\tORDER BY sstkno ASC LIMIT %s", getStringArray(selected), limit);

        List<HashMap<String, Object>> products = DbUtils.query(ctx, sql);
        return this.getFromList(products);
    }

    @Override
    public List<Product> getByColumn(String column, String value, Integer limit) {
        String sql = String.format("\n" +
                "SELECT sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tFROM tbsstock\n" +
                "\tWHERE %s = '%s' \n" +
                "\tORDER BY sstkno ASC LIMIT %s", column, value, limit);

        List<HashMap<String, Object>> products = DbUtils.query(ctx, sql);
        return this.getFromList(products);
    }


    @Override
    public List<Product> getByRange(String from, String to, Integer limit) {
        String sql = String.format("\n" +
                "SELECT sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tFROM tbsstock\n" +
                "\tWHERE sstkno \n" +
                "\tBETWEEN '%s' AND '%s' ORDER BY sstkno ASC LIMIT %s", from, to, limit);

        List<HashMap<String, Object>> products = DbUtils.query(ctx, sql);
        return this.getFromList(products);
    }


    public List<HashMap<String, Object>> getAllProducts() {
        String sql = String.format("\n" +
                "select sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tfrom tbsstock limit 200\n"
        );

        List<HashMap<String, Object>> product = DbUtils.query(ctx, sql);
        return product;
    }

    public List<HashMap<String, Object>> getProductInfoByProductID(String pid) {
        String sql = String.format("\n" +
                "select sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tfrom tbsstock \n" +
                "\twhere sstkno='%s'", pid
        );

        List<HashMap<String, Object>> product = DbUtils.query(ctx, sql);
        return product;
    }

    public List<HashMap<String, Object>> getProductByPrefix(String prefix, Integer limit) {
        String sql = String.format("\n" +
                "SELECT sstkno, sstkname, sstkcolor, scustno, sclassno \n" +
                "\tFROM tbsstock\n" +
                "\tWHERE sstkno \n" +
                "\tLIKE '%s%%' ORDER BY sstkno ASC LIMIT %s", prefix, limit);

        List<HashMap<String, Object>> product = DbUtils.query(ctx, sql);
        return product;
    }

}

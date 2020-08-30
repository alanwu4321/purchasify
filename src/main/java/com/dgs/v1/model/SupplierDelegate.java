package com.dgs.v1.model;

import com.dgs.v1.service.Context;
import com.dgs.v1.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;


public class SupplierDelegate extends Delegate<Supplier> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDelegate.class);
    private final Context ctx;

    public SupplierDelegate(Context ctx){
        super(Supplier.class);
        this.ctx = ctx;
    }

    @Override
    public List<Supplier> getByPrefix(String prefix, Integer limit) {
        String sql = String.format("\n" +
                "SELECT scustno, scustname, skind \n" +
                "\tFROM tbscust\n" +
                "\tWHERE skind = '1' AND scustno LIKE '%s%%'\n" +
                "\tORDER BY scustno ASC\n" +
                "\tLIMIT %s\n", prefix, limit
        );

        List<HashMap<String, Object>> suppliers = DbUtils.query(ctx, sql);
        return this.getFromList(suppliers);
    }
}

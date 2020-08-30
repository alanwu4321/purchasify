package com.dgs.v1.model;

import com.dgs.v1.service.Context;
import com.dgs.v1.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class CategoryDelegate extends Delegate<Category> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDelegate.class);
    private final Context ctx;

    public CategoryDelegate(Context ctx) {
        super(Category.class);
        this.ctx = ctx;
    }

    @Override
    public List<Category> getByPrefix(String prefix, Integer limit) {
        String sql = String.format("\n" +
                "SELECT sclassno, sclassname \n" +
                "\tFROM tbsstkclass\n" +
                "\tWHERE sclassno \n" +
                "\tLIKE '%s%%' ORDER BY sclassno ASC LIMIT %s", prefix, limit);

        List<HashMap<String, Object>> categories = DbUtils.query(ctx, sql);
        return this.getFromList(categories);
    }
}

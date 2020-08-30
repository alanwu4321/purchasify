package com.dgs.v1.model;



import com.dgs.v1.service.Context;
import com.dgs.v1.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class ProductDemandDelegate extends Delegate<ProductDemand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDemandDelegate.class);
    private final Context ctx;

    public ProductDemandDelegate(Context ctx){
        super(ProductDemand.class);
        this.ctx = ctx;
    }

    @Override
    public List<ProductDemand> getById(String pid) {
        String sql = String.format("\n" +
                "SELECT to_char(date_trunc('month', dtslipdate), 'YYYY-MM') as date, " +
                "\tsum(fstkqty * fqtyrate * CASE skind \n" +
                "       WHEN '1' THEN 1 \n" +
                "       WHEN '2' THEN -1 \n" +
                "   END) as monthlydemand\n" +
                "   FROM tbsslipdtx\n" +
                "\t where sstkno='%s'\n" +
                "\t and dtslipdate >\n" +
                "      date_trunc('month', CURRENT_DATE) - INTERVAL '12 month' \n" +
                "   and dtslipdate <= \n" +
                "      date_trunc('month', CURRENT_DATE)\n" +
                "   and shouseno in (%s)    \n " +
                " GROUP BY date_trunc('month', dtslipdate) \n" +
                " order by date asc\n" +
                "\n", pid, InventoryDelegate.getHouseFilterQueryFromCtx(ctx));

        List<HashMap<String, Object>> productDemands = DbUtils.query(ctx, sql);
        return this.getFromList(productDemands);
    }

    @Override
    public List<ProductDemand> getByPrefix(String prefix, Integer limit) {
        return null;
    }
}

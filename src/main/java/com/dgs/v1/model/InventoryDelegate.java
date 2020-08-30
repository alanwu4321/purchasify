package com.dgs.v1.model;

import com.dgs.v1.service.Context;
import com.dgs.v1.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class InventoryDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDelegate.class);
    private Context ctx;

    public InventoryDelegate(Context ctx) {
        this.ctx = ctx;
    }

    //and shouseno in ('A', 'B')
    public static String getHouseFilterQueryFromCtx(Context ctx) {
        String[] warehouses = ctx.getWarehouseFilter();
        StringBuilder str = new StringBuilder();
        String prefix = "";
        for (String w : warehouses) {
            str.append(prefix);
            prefix = ",";
            str.append('\'' + w + "'");
        }
        return str.toString();
    }


    public List<HashMap<String, Object>> queryOrderYetPickedByPid(String pid) {
        String sql = String.format("select ((tbsslipdto.fstkqty * tbsslipdto.fqtyrate) - tbsslipdto.fdoneqty) as %s, \n" +
                        "tbscust.scustname,\n" +
                        "tbsslipdto.shouseno,\n" +
                        "tbsslipdto.sslipno,tbsslipdto.scustno,sunit,tbsslipdto.dtslipdate,\n" +
                        "fstkqty,fdoneqty,tbsslipdto.srem1,fqtyrate,tbsslipdto.dtvaliddate,tbsslipo.spackflag\n" +
                        "from tbsslipdto \n" +
                        "inner join tbscust on tbscust.scustno=tbsslipdto.scustno \n" +
                        "inner join tbsslipo on tbsslipdto.sslipno=tbsslipo.sslipno \n" +
                        "where sstkno='%s' \n" +
                        "and tbsslipo.spackflag='0' \n" +
                        "and tbsslipo.skind='E' \n" +
                        "and tbsslipdto.skind='E' \n" +
                        "and tbsslipo.spackflag !='1'\n" +
                        "and (tbsslipdto.fstkqty * tbsslipdto.fqtyrate) - tbsslipdto.fdoneqty > 0\n" +
                        "and tbsslipdto.shouseno in (%s)\n ",
                Inventory.amount,pid, getHouseFilterQueryFromCtx(ctx)
        );

        return DbUtils.query(ctx, sql);
    }

    public List<HashMap<String, Object>> queryPurchasedYetReceivedByPid(String pid) {
        String sql = String.format(
                "select ((tbsslipdto.fstkqty * tbsslipdto.fqtyrate) - tbsslipdto.fdoneqty) as %s, \n" +
                        "tbscust.scustname,\n" +
                        "tbsslipdto.shouseno,\n" +
                        "tbsslipdto.sslipno,tbsslipdto.scustno,sunit,tbsslipdto.dtslipdate,foldprice,\n" +
                        "fstkqty,fstotal,fdoneqty,tbsslipdto.srem1, fqtyrate,tbsslipdto.dtvaliddate,tbsslipo.spackflag\n" +
                        "from tbsslipdto \n" +
                        "inner join tbscust on tbscust.scustno=tbsslipdto.scustno \n" +
                        "inner join tbsslipo on tbsslipdto.sslipno=tbsslipo.sslipno \n" +
                        "where sstkno='%s' \n" +
                        "and tbsslipo.spackflag='0' \n" +
                        "and tbsslipo.skind='F' \n" +
                        "and tbsslipdto.skind='F' \n" +
                        "and tbsslipo.spackflag !='1'\n" +
                        "and (tbsslipdto.fstkqty * tbsslipdto.fqtyrate) - tbsslipdto.fdoneqty > 0\n" +
                        "and tbsslipdto.shouseno in (%s)\n ", Inventory.amount, pid, getHouseFilterQueryFromCtx(ctx)
        );

        return DbUtils.query(ctx, sql);
    }

    public List<HashMap<String, Object>> queryTotalStock(String pid) {
        String sql = String.format("select tbsstkhouse.fcurqty as %s,  \n" +
                        "tbsstkhouse.shouseno, \n" +
                        "tbsstkhouse.sstkno from tbsstkhouse \n" +
                        "where tbsstkhouse.sstkno='%s'\n" +
                        "and tbsstkhouse.shouseno in (%s)\n" +
                        "order by shouseno asc\n",
                Inventory.amount, pid, getHouseFilterQueryFromCtx(ctx));

        return DbUtils.query(ctx, sql);
    }

}

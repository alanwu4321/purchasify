package com.dgs.v1.model;

import com.dgs.v1.service.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;

public class Product extends Entity<Product> {
    private String sstkno;      //貨品編號
    private String sstkname;  //貨品名稱
    private String sstkcolor; //採購模式
    private String scustno;   //供應商編號
    private String sclassno;  //貨品類別編號
    private long timeToLive;

    public Product(String sstkno, String sstkname, String sstkcolor, String scustno, String sclassno) {
        this.sstkno = sstkno;
        this.sstkname = sstkname;
        this.sstkcolor = sstkcolor;
        this.scustno = scustno;
        this.sclassno = sclassno;
    }

    public Product() {
        super();
    }

    public static Product buildProductFromMap(List<HashMap<String, Object>> Lmap) {
        HashMap<String, Object> map = Lmap.get(0);
        return new Product(
                (String) map.get("sstkno"),
                (String) map.get("sstkname"),
                (String) map.get("sstkcolor"),
                (String) map.get("scustno"),
                (String) map.get("sclassno")
        );
    }

    @Override
    public Delegate<Product> getDelegate(Context ctx) {
        return new ProductDelegate(ctx);
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public String getSstkno() {
        return sstkno;
    }

    public void setSstkno(String sstkno) {
        this.sstkno = sstkno;
    }

    public String getSstkname() {
        return sstkname;
    }

    public void setSstkname(String sstkname) {
        this.sstkname = sstkname;
    }

    public String getSstkcolor() {
        return sstkcolor;
    }

    public void setSstkcolor(String sstkcolor) {
        this.sstkcolor = sstkcolor;
    }

    public String getScustno() {
        return scustno;
    }

    public void setScustno(String scustno) {
        this.scustno = scustno;
    }

    public String getSclassno() {
        return sclassno;
    }

    public void setSclassno(String sclassno) {
        this.sclassno = sclassno;
    }
}

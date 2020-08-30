package com.dgs.v1.model;

import com.dgs.v1.service.Context;

public class Category extends Entity {

    private String sclassno;    //貨品類別編號

    private String sclassname; //貨品類別名稱

    public Category(String sclassno, String sclassname) {
        this.sclassno = sclassno;
        this.sclassname = sclassname;
    }

    @Override
    public Delegate getDelegate(Context ctx) {
        return new CategoryDelegate(ctx);
    }

    public Category() {
        super();
    }

    public String getSclassno() {
        return sclassno;
    }

    public void setSclassno(String sclassno) {
        this.sclassno = sclassno;
    }

    public String getSclassname() {
        return sclassname;
    }

    public void setSclassname(String sclassname) {
        this.sclassname = sclassname;
    }


}
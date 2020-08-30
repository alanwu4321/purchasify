package com.dgs.v1.model;

import com.dgs.v1.service.Context;

public class Supplier extends Entity<Supplier>{
    //skind 1 => supplier / 2 => client

    private String scustno; //客戶/廠商 供應商編號

    private String scustname; //客戶/廠商 供應商名稱

    private String skind; // 標誌

    public Supplier(String scustno, String scustname, String skind) {
        this.scustno = scustno;
        this.scustname = scustname;
        this.skind = skind;
    }

    public Supplier() {
        super();
    }

    @Override
    public Delegate<Supplier> getDelegate(Context ctx) {
        return new SupplierDelegate(ctx);
    }

    public String getScustno() {
        return scustno;
    }

    public void setScustno(String scustno) {
        this.scustno = scustno;
    }

    public String getScustname() {
        return scustname;
    }

    public void setScustname(String scustname) {
        this.scustname = scustname;
    }

    public String getSkind() {
        return skind;
    }

    public void setSkind(String skind) {
        this.skind = skind;
    }
}


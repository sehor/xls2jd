package com.pzr.xls2jd.core.domain.batchInvocie;

import lombok.Data;

/**
 * @Author pzr
 * @date:2023-02-15-8:05
 * @Description:
 **/
@Data
public class InvoiceItem {
    private String itemName;
    private String modelAndType;
    private double amount;
    private double quantity;
    private double price;
    private double taxRate;

    private String itemTaxNum;

    private String unitType="个";


    /*企业商品编码*/
    private String Qyspbm="";

    /*是否使用优惠政策标识 0：不使用，1：使用（1字节）*/
    private String Syyhzcbz="";
    /*零税率标识 空：非零税率，0：出口退税，1：免税，2：不征收，3普通零税率（1字节）*/
    private String Lslbz="";
    /*优惠政策说明（50字节）*/
    private String Yhzcsm="";
    /* 扣除额，用于差额税计算*/
    private double Kce;


    public double getAmount() {
        return quantity * price;
    }
    public InvoiceItem copy(){
        InvoiceItem item=new InvoiceItem();
        item.setItemName(this.itemName);
        item.setKce(this.Kce);
        item.setItemTaxNum(this.itemTaxNum);
        item.setModelAndType(this.modelAndType);
        item.setPrice(this.price);
        item.setQuantity(this.quantity);
        item.setQyspbm(this.Qyspbm);
        item.setSyyhzcbz(this.Syyhzcbz);
        item.setTaxRate(this.taxRate);
        item.setLslbz(this.Lslbz);
        item.setYhzcsm(this.Yhzcsm);
        item.setUnitType(this.unitType);
        item.setAmount(this.getAmount());
        return item;
    }

}

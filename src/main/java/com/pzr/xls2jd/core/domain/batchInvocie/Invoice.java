package com.pzr.xls2jd.core.domain.batchInvocie;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author pzr
 * @date:2023-02-15-8:23
 * @Description:
 **/
@Data
public class Invoice {
  private   List<InvoiceItem> items=new ArrayList<>();
  private double total;

  /*备注*/
  private Buyer buyer;
  private String notes;
  private String reviewer;
  private String payee;

  /*商品编码版本号*/
  private String codeVersion;

  /*含税标志*/
  private String taxFlag;


  public double getTotal(){
    double sum = 0.0;
    for (InvoiceItem item : items) {
      sum+=item.getAmount();
    }
    return sum;
  }

  public Invoice copy(){
    Invoice invoice=new Invoice();
    invoice.setBuyer(this.buyer);
    invoice.setCodeVersion(this.codeVersion);
    invoice.setNotes(this.notes);
    invoice.setPayee(this.payee);
    invoice.setReviewer(this.reviewer);
    invoice.setTaxFlag(this.taxFlag);
    invoice.setTotal(this.getTotal());
    List<InvoiceItem> items=new ArrayList<>();
    for (InvoiceItem item : this.items) {
      items.add(item.copy());
    }
    invoice.setItems(items);
    return invoice;
  }

}

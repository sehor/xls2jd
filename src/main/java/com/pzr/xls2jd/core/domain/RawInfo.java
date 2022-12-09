package com.pzr.xls2jd.core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author pzr
 * @date:2022-12-04-11:10
 * @Description:
 **/
public class RawInfo {
    private static final long serialVersionUID = 1L;

    private String serial_number;

    private String id;

    private String type = "No_Handled";

    private String brief = "";

    private Double amout = (double) 0;

    private Double bank_income = (double) 0;;

    private Double bank_pay = (double) 0;;

    private String relative_account = "";

    private LocalDate occur_date;

    private String companyName;
    private String bank_brief1 = "";
    private String bank_brief2 = "";

    private String relative_account_number;

    private Double basicSalary = (double) 0;
    private Double salary_other = (double) 0;
    private Double salary_security = (double) 0;
    private Double salary_funds = (double) 0;
    private Double salary_personTax = (double) 0;
    private Double salary_payable = (double) 0;

    private String invoice_number;
    private LocalDate invoice_date;
    private Double invoice_amount = (double) 0;
    private Double invoice_tax = (double) 0;

    private Double payedPersonSecurity = (double) 0;
    private Double payedCompanySecurity = (double) 0;

    private String income_account; // 对应的type为Issue_invoice的origin应计入哪个收入科目
    private String payable_account; // 对应的type为Receive_invoice的origin应计入哪个成本费用科目

    private Double bill_income; // 收到汇票
    private Double bill_pay; // 背书发出汇票

    private String bankNum="100201"; //有两个或以上银行的，要区分
    private String relative_bankNum=""; //对方银行账号，主要用来处理同一公司间互相转账

    public Double getBill_income() {
        return bill_income;
    }

    public void setBill_income(Double bill_income) {
        this.bill_income = new BigDecimal(bill_income).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

    }

    public Double getBill_pay() {
        return bill_pay;
    }

    public void setBill_pay(Double bill_pay) {
        this.bill_pay = new BigDecimal(bill_pay).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    public LocalDate getInvoice_date() {
        return invoice_date;
    }

    public void setInvoice_date(LocalDate invoice_date) {
        this.invoice_date = invoice_date;
    }

    public Double getInvoice_amount() {
        return invoice_amount;
    }

    @SuppressWarnings("deprecation")
    public void setInvoice_amount(Double invoice_amount) {
        this.invoice_amount = new BigDecimal(invoice_amount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public Double getInvoice_tax() {
        return invoice_tax;
    }

    public void setInvoice_tax(Double invoice_tax) {
        this.invoice_tax = new BigDecimal(invoice_tax).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public Double getSalary_payable() {
        return salary_payable;
    }

    public void setSalary_payable(Double salary_payable) {
        this.salary_payable = new BigDecimal(salary_payable).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = new BigDecimal(basicSalary).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public Double getSalary_other() {
        return salary_other;
    }

    public void setSalary_other(Double salary_other) {
        this.salary_other = new BigDecimal(salary_other).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public Double getSalary_security() {
        return salary_security;
    }

    public void setSalary_security(Double salary_security) {
        this.salary_security = new BigDecimal(salary_security).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public Double getSalary_funds() {
        return salary_funds;
    }

    public void setSalary_funds(Double salary_funds) {
        this.salary_funds = new BigDecimal(salary_funds).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public Double getSalary_personTax() {
        return salary_personTax;
    }

    public void setSalary_personTax(Double salary_personTax) {
        this.salary_personTax = new BigDecimal(salary_personTax).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        ;
    }

    public String getRelative_account_number() {
        return relative_account_number;
    }

    public void setRelative_account_number(String relative_account_number) {
        this.relative_account_number = relative_account_number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public Double getAmout() {
        return amout;
    }

    public void setAmout(Double amout) {
        this.amout = new BigDecimal(amout).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public Double getBank_income() {
        return bank_income;
    }

    public void setBank_income(Double bank_income) {
        this.bank_income = new BigDecimal(bank_income).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public Double getBank_pay() {
        return bank_pay;
    }

    public void setBank_pay(Double bank_pay) {
        this.bank_pay = new BigDecimal(bank_pay).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public String getRelative_account() {
        return relative_account;
    }

    public void setRelative_account(String relative_account) {
        this.relative_account = relative_account;
    }

    public LocalDate getOccur_date() {
        return occur_date;
    }

    public void setOccur_date(LocalDate occur_date) {
        this.occur_date = occur_date;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBank_brief1() {
        return bank_brief1;
    }

    public void setBank_brief1(String bank_brief1) {
        this.bank_brief1 = bank_brief1;
    }

    public String getBank_brief2() {
        return bank_brief2;
    }

    public void setBank_brief2(String bank_brief2) {
        this.bank_brief2 = bank_brief2;
    }

    public Double getPayedPersonSecurity() {
        return payedPersonSecurity;
    }

    @SuppressWarnings("deprecation")
    public void setPayedPersonSecurity(Double payedPersonSecurity) {
        this.payedPersonSecurity = new BigDecimal(payedPersonSecurity).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    public Double getPayedCompanySecurity() {
        return payedCompanySecurity;
    }

    @SuppressWarnings("deprecation")
    public void setPayedCompanySecurity(Double payedCompanySecurity) {
        this.payedCompanySecurity = new BigDecimal(payedCompanySecurity).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    public String getIncome_account() {
        return income_account;
    }

    public void setIncome_account(String income_account) {
        this.income_account = income_account;

    }

    public String getPayable_account() {
        return payable_account;
    }

    public void setPayable_account(String payable_account) {
        this.payable_account = payable_account;
    }






    public String getBankNum() {
        return bankNum;
    }

    public void setBankNum(String bankNum) {
        this.bankNum = bankNum;
    }

    public RawInfo clone() {
        // TODO Auto-generated method stub
        RawInfo copy = null;

        try {
            copy = (RawInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return copy;
    }

    public String getRelative_bankNum() {
        return relative_bankNum;
    }

    public void setRelative_bankNum(String relative_bankNum) {
        this.relative_bankNum = relative_bankNum;
    }


    @Override
    public String toString() {

        return "brief: "+this.getBank_brief1()+" "+this.getBank_brief2()+" amount"+this.getBank_income();
    }

}

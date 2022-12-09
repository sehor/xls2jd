package com.pzr.xls2jd.core.domain;

/**
 * @Author pzr
 * @date:2022-12-04-17:53
 * @Description:
 **/
public enum  BriefKeyWord {

    Bank_Interest("bankInterest"),

    Bank_Salary("bankSalary"),
    Bank_Tax("bankTax"),
    Bank_Security("bankSecurity"),
    Bank_HPFund("bankHPFund"),
    Bank_Fee("bankFee"),

    BanK_Payment_For_Good("bankPayMentForGood"),
    Bank_Receive_Payment("bankReceivePayment"),

    Bank_Expense("bankExpense"),
    Bank_VATAndAdditionalTax("bankVATAndAdditionalTax"),
    Bank_VAT("bankVAT"),
    Bank_IncomeTax("bankIncomeTax"),
    Bank_PersonalTax("bankPersonalTax");

    public final String val;
    private BriefKeyWord(String val) {
        this.val=val;
    }
}

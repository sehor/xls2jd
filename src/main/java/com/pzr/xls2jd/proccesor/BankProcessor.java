package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.util.Util;
import com.pzr.xls2jd.core.domain.AccountPeriod;
import com.pzr.xls2jd.core.domain.BriefKeyWord;
import com.pzr.xls2jd.core.domain.TypeEnum;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author pzr
 * @date:2022-12-04-18:03
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class BankProcessor implements Processor {
    private final ProcessorService service;

    public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {

        List<RawInfo> bankInfos = Util.filterAndCopy(origins, e -> e.getType().contains(TypeEnum.BanK.value));
        Map<String, String> bankMap = service.getBankMap();

        for (RawInfo origin : bankInfos) {

            origin.setAmout(origin.getBank_income() + origin.getBank_pay());
            origin.setBrief(origin.getBank_brief1() + " " + origin.getBank_brief2()+" "+origin.getBrief());
            System.out.println(companyName);

            // 公司银行间转账(不是结息)
            if (origin.getRelative_account()!=null&&origin.getRelative_account().contains(companyName)
                    && !service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Interest.val)) {
                origin.setType(TypeEnum.Bank_Tranfer_Interal.value);
                String value = bankMap.get(origin.getRelative_bankNum());
                // origin.setRelative_account(value);
                origin.setRelative_account_number(value);
                continue;
            }

            String 应收账款类的科目 = service.getAccountNumByMutilName("应收账款-" + origin.getRelative_account(),
                    companyName);
            String 应付账款类的科目 = service.getAccountNumByMutilName("应付账款-" + origin.getRelative_account(),
                    companyName);
            String 其他应收款的科目 = service.getAccountNumByMutilName("其他应收款-" + origin.getRelative_account(),
                    companyName);
            String 其他应付款的科目 = service.getAccountNumByMutilName("其他应付款-" + origin.getRelative_account(),
                    companyName);
            String 其他应付款_其他 = service.getAccountNumByMutilName("其他应付款-其他", companyName);
            String 其他应收款_其他 = service.getAccountNumByMutilName("其他应收款-其他", companyName);

            String nonPayAndReceive = service.getAccountNumByMutilName(origin.getRelative_account(),
                    companyName);
            if (!nonPayAndReceive.equals("未找到")) {
                origin.setRelative_account_number(nonPayAndReceive);
                continue;
            }

            // bank income，收款是收款，按应收账款-.>其他应收款-,>应付账款->其他应付款的顺序赋值，如果是付款，顺序反之

            //收款
            if (origin.getBank_income() >= 0.01) {

                if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Interest.val)) {
                    origin.setType(TypeEnum.Bank_Income_Interest.value);
                    origin.setRelative_account("银行费用");
                    origin.setRelative_account_number(
                            service.getAccountNumByMutilName("财务费用-利息", companyName));
                } else if (!应收账款类的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Income_Receivable.value);
                    origin.setRelative_account_number(应收账款类的科目);
                }
                //货款，但还没录入科目的
                else if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Receive_Payment.val)||origin.getBrief().contains("货")) {
                    origin.setType(TypeEnum.Bank_Income_Receivable.value);
                    origin.setRelative_account_number("未找到");
                }
                //可能是预付
                else if (!应付账款类的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Income_Payable.value);
                    origin.setRelative_account_number(应付账款类的科目);
                }

                else if (!其他应收款的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Income_OtherReceivable.value);
                    origin.setRelative_account_number(其他应收款的科目);
                }
                else if (!其他应付款的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Income_OtherPayable.value);
                    origin.setRelative_account_number(其他应付款的科目);
                }
                else {

                    origin.setType(TypeEnum.Bank_Income_NotFound.value);
                    origin.setRelative_account_number(TypeEnum.Bank_Income_NotFound.value+": "+origin.getRelative_account());

                }
            }
            //付款。。
            else if (origin.getBank_pay() >= 0.01) {
                if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Fee.val)) { // 银行费用
                    origin.setBrief("银行收费");
                    origin.setRelative_account("财务费用-手续费");
                    origin.setRelative_account_number(
                            service.getAccountNumByMutilName("财务费用-手续费", companyName));
                    origin.setType(TypeEnum.Bank_Pay_BankFee.value);
                }
                else if (!应付账款类的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Pay_Payable.value);
                    origin.setRelative_account_number(应付账款类的科目);
                }
                //货款，没录入的供应商
                else if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.BanK_Payment_For_Good.val)||origin.getBrief().contains("货")) {
                    origin.setType(TypeEnum.Bank_Pay_Payable.value);
                    origin.setRelative_account_number("未找到: "+origin.getRelative_account());
                }
                else if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Salary.val)) { // 发放工资
                    origin.setRelative_account("应付职工薪酬-应付工资");
                    origin.setRelative_account_number(
                            service.getAccountNumByMutilName("应付职工薪酬-应付工资", companyName));
                    origin.setType(TypeEnum.Bank_Pay_Salary.value);
                }
                else if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Tax.val)) { // 缴税
                    origin.setRelative_account("应付税费"); // 先设置成应付税费
                    origin.setRelative_account_number("");// 待定
                    origin.setType(TypeEnum.Bank_Pay_Tax.value);
                }
                else if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_Security.val)) { // 社保
                    origin.setRelative_account("其他应付款-社保");
                    origin.setRelative_account_number("");// 待定
                    origin.setType(TypeEnum.Bank_Pay_SocialSecurity.value);
                }
                else if (service.isContainKeyWord(origin.getBrief(), BriefKeyWord.Bank_HPFund.val)) { // 公积金
                    origin.setRelative_account("应付公积金");
                    origin.setRelative_account_number("");// 待定
                    origin.setType(TypeEnum.Bank_Pay_Housing_Provident_Fund.value);
                }
                 else if (!应收账款类的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Pay_Receivable.value);
                    origin.setRelative_account_number(应收账款类的科目);

                }
                 else if (!其他应付款的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Pay_OtherPayable.value);
                    origin.setRelative_account_number(其他应付款的科目);
                }
                 else if (!其他应收款的科目.equals("未找到")) {
                    origin.setType(TypeEnum.Bank_Pay_OtherReceivable.value);
                    origin.setRelative_account_number(其他应收款的科目);
                }
                 else {
                    origin.setType(TypeEnum.Bank_Pay_NotFound.value);
                    origin.setRelative_account_number(TypeEnum.Bank_Pay_NotFound.value+": "+origin.getRelative_account());
                }
            }
        }
        return bankInfos;

    }

    /*
     * 有相同type 和 relative account number 的origins will be merged into one ,
     *
     */
    private void mergeSame(List<RawInfo> origins,List<RawInfo> merges) {

        Map<String, RawInfo> map = new HashMap<>();
        for (RawInfo origin : merges) {
            RawInfo mergeOrigin = map.get(origin.getType() + origin.getBankNum() + origin.getRelative_account_number()); // 以type,bankNum和relative
            // account
            // number为key
            // merge
            if (mergeOrigin == null) {
                map.put(origin.getType() + origin.getBankNum() + origin.getRelative_account_number(), origin);
            } else {
                mergeOrigin.setAmout(origin.getAmout() + mergeOrigin.getAmout());
                mergeOrigin.setBank_income(mergeOrigin.getBank_income() + origin.getBank_income());
                mergeOrigin.setBank_pay(mergeOrigin.getBank_pay() + origin.getBank_pay());
                mergeOrigin.setOccur_date(
                        origin.getOccur_date().isAfter(mergeOrigin.getOccur_date()) ? origin.getOccur_date()
                                : mergeOrigin.getOccur_date()); // 选最晚的date
            }
        }

        origins.removeAll(merges);
        origins.addAll(map.values());

    }


    public List<AccountEntry> processToRecord(List<RawInfo> rawInfos,String companyName) {
        List<RawInfo> merges = Util.filter(rawInfos, service::needToBeMerged);
        mergeSame(rawInfos,merges);


        List<AccountEntry> aEntries = new ArrayList<>();

        //先处理公司内部银行转账
        List<RawInfo> internals = Util.filter(rawInfos, e -> e.getType().equals(TypeEnum.Bank_Tranfer_Interal.value));
        aEntries.addAll(handleBankTransferInternal(internals)); //公司内部银行间转账

        rawInfos.removeAll(internals);

        // 把不同银行的origin分开来处理
        Map<String, List<RawInfo>> map = new HashMap<>();
        for (RawInfo origin : rawInfos) {
            map.computeIfAbsent(origin.getBankNum(), k -> new ArrayList<>());
            map.get(origin.getBankNum()).add(origin);

        }


        for (List<RawInfo> origins : map.values()) {

            Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Pay_Tax.value))
                    .forEach(e -> aEntries.addAll(this.handleBankPayTax(e,companyName)));// 处理缴税

            Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Pay_SocialSecurity.value))
                    .forEach(e -> aEntries.addAll(this.handleBankPaySecurity(e,companyName)));// 处理社会保险

            Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Pay_Housing_Provident_Fund.value))
                    .forEach(e -> aEntries.addAll(this.handleBankPayHousingProvidentFund(e,companyName)));// 处理公积金

            aEntries.addAll(handleBankGroupItems(
                    Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Income_Receivable.value)))); // 收货款，合并分录
            aEntries.addAll(handleBankGroupItems(
                    Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Pay_Payable.value)))); // 付款，合并分录
            aEntries.addAll(
                    handleBankGroupItems(Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Pay_Other.value)))); // 其他应付款，合并分录

            aEntries.addAll(handleBankGroupItems(
                    Util.filter(origins, e -> e.getType().equals(TypeEnum.Bank_Pay_OtherPayable.value)))); // 其他付款，合并分录

            List<String> excludType = List.of(TypeEnum.Bank_Pay_Tax.value, TypeEnum.Bank_Pay_SocialSecurity.value,
                    TypeEnum.Bank_Income_Receivable.value, TypeEnum.Bank_Pay_Payable.value,
                    TypeEnum.Bank_Pay_Other.value, TypeEnum.Bank_Pay_Housing_Provident_Fund.value,
                    TypeEnum.Bank_Pay_OtherPayable.value);

            for (RawInfo origin : Util.filter(origins, e -> !excludType.contains(e.getType()))) {
                aEntries.add(createEntry(origin));
            }

        }

        return aEntries;
    }

    private List<AccountEntry> handleBankPayTax(RawInfo origin,String companyName) {

        AccountEntry entry = new AccountEntry();

        // 增值税和附加税混合的缴税
        float CJ_Tax_Rate = 0.07f;
        float JY_Tax_Rate = 0.03f;
        float DFJY_Tax_Rate = 0.02f;
        if (service.isContainKeyWord(origin.getBrief(), "bankVATAndAdditionalTax")) {
            double rate = CJ_Tax_Rate + JY_Tax_Rate + DFJY_Tax_Rate;
            double vatMount = origin.getAmout() / (1 + rate);
            Record vta_record = Util.createRecord(origin); // 增值税
            vta_record.set借方金额(vatMount);
            vta_record.set科目名称("未交增值税");
            vta_record.set摘要("缴纳税费");
            vta_record.set科目代码(service.getAccountNumByMutilName("应交税费-未交增值税", companyName));

            Record cj_record = Util.createRecord(origin);
            cj_record.set摘要("缴纳税费");
            cj_record.set借方金额(vatMount * CJ_Tax_Rate);
            cj_record.set科目名称("应交城市维护建设税");
            cj_record.set科目代码(service.getAccountNumByMutilName("应交税费-应交城市维护建设税", companyName));

            Record jy_record = Util.createRecord(origin);
            jy_record.set摘要("缴纳税费");
            jy_record.set借方金额(vatMount * JY_Tax_Rate);
            jy_record.set科目名称("教育费附加");
            jy_record.set科目代码(service.getAccountNumByMutilName("应交税费-教育费附加", companyName));

            Record dfjy_record = Util.createRecord(origin);
            dfjy_record.set摘要("缴纳税费");
            // dfjy_record.set借方金额(vatMount *this.DFJY_Tax_Rate); //借贷有误差
            dfjy_record.set借方金额(origin.getAmout() - vta_record.get借方金额()-cj_record.get借方金额()-jy_record.get借方金额());// 借贷有误差,在地方教育附加调整
            dfjy_record.set科目名称("地方教育费附加");
            dfjy_record.set科目代码(service.getAccountNumByMutilName("应交税费-地方教育费附加", companyName));

            // entry.addAll(List.of(vta_record, cj_record, jy_record, dfjy_record));
            entry.add(vta_record);
            entry.add(cj_record);
            entry.add(jy_record);
            entry.add(dfjy_record);

        }
        // 单独增值税
        else if (service.isContainKeyWord(origin.getBrief(), "bankVAT")) {
            Record vta_record = Util.createRecord(origin); // 增值税
            vta_record.set摘要("缴纳税费");
            vta_record.set借方金额(origin.getAmout());
            vta_record.set科目名称("未交增值税");
            vta_record.set科目代码(service.getAccountNumByMutilName("应交税费-未交增值税", companyName));
            entry.add(vta_record);
        }
        // 附加税
        else if (service.isContainKeyWord(origin.getBrief(), "bankAdditionanlTax")) {
            double rate = CJ_Tax_Rate + JY_Tax_Rate + DFJY_Tax_Rate;
            double amout = origin.getAmout();
            Record cj_record = Util.createRecord(origin);
            cj_record.set摘要("缴纳税费");
            cj_record.set借方金额(amout * CJ_Tax_Rate / rate);
            cj_record.set科目名称("应交城市维护建设税");
            cj_record.set科目代码(service.getAccountNumByMutilName("应交税费-应交城市维护建设税", companyName));

            Record jy_record = Util.createRecord(origin);
            jy_record.set摘要("缴纳税费");
            jy_record.set借方金额(amout * JY_Tax_Rate / rate);
            jy_record.set科目名称("教育费附加");
            jy_record.set科目代码(service.getAccountNumByMutilName("应交税费-教育费附加", companyName));

            Record dfjy_record = Util.createRecord(origin);
            dfjy_record.set摘要("缴纳税费");
            // dfjy_record.set借方金额(amout * this.DFJY_Tax_Rate / rate);
            dfjy_record.set借方金额(origin.getAmout() - cj_record.get借方金额() - jy_record.get借方金额());// 借贷有误差,在地方教育附加调整
            dfjy_record.set科目名称("地方教育费附加");
            dfjy_record.set科目代码(service.getAccountNumByMutilName("应交税费-地方教育费附加", companyName));

            // entry.addAll(List.of(cj_record, jy_record, dfjy_record));
            entry.add(cj_record);
            entry.add(jy_record);
            entry.add(dfjy_record);
        }
        // 企业所得税
        else if (service.isContainKeyWord(origin.getBrief(), "bankIncomeTax")) {
            Record qysd_record = Util.createRecord(origin);
            qysd_record.set摘要("缴纳税费");
            qysd_record.set借方金额(origin.getAmout());
            qysd_record.set科目名称("所得税费用");
            qysd_record.set科目代码(service.getAccountNumByMutilName("所得税费用", companyName));

            entry.add(qysd_record);
        }
        // 个人所得税
        else if (service.isContainKeyWord(origin.getBrief(), "bankPersonalTax")) {
            Record grsd_record = Util.createRecord(origin);
            grsd_record.set摘要("缴纳税费");
            grsd_record.set借方金额(origin.getAmout());
            grsd_record.set科目名称("应交个人所得税");
            grsd_record.set科目代码(service.getAccountNumByMutilName("应交税费-应交个人所得税", companyName));

            entry.add(grsd_record);
        }
        // 其他归集到管理费用税金
        else {

            Record iq_record = Util.createRecord(origin);
            iq_record.set摘要("缴纳税费");
            iq_record.set借方金额(origin.getAmout());
            iq_record.set科目名称("归集为管理费的税费");
            iq_record.set科目代码(service.getAccountNumByMutilName("管理费用-归集为管理费的税费", companyName));

            entry.add(iq_record);
        }

        // 贷方的银行记录
        Record bank_record = Util.createBankRecord(origin);
        bank_record.set摘要("缴纳税费");
        entry.add(bank_record);

        return List.of(entry);
    }

    private List<AccountEntry> handleBankPaySecurity(RawInfo origin,String companyName) {
        // TODO Auto-generated method stub
        AccountEntry entry = new AccountEntry();

        AccountPeriod period = new AccountPeriod(origin.getOccur_date());

        double personSecurity = service.findPersonSecurity(companyName, period.getBeginDay(),
                    period.getLastDay());
        
        Record person_security = Util.createRecord(origin);
        person_security.set科目名称("应交社会保险费（个人）");
        person_security.set科目代码(service.getAccountNumByMutilName("其他应付款-应交社会保险费（个人）", companyName));
        // String personSecurityAmout =
        // companyProperties.getCompanies().get(this.companyName).get("personSecurityAmout");
        // person_security.set借方金额(origin.getBank_pay()/3); //大约总是的1/3
        person_security.set借方金额(personSecurity);

        Record company_security = Util.createRecord(origin);
        company_security.set科目名称("应交社会保险费（单位）");
        company_security.set科目代码(service.getAccountNumByMutilName("其他应付款-应交社会保险费（单位）", companyName));
        company_security.set借方金额(origin.getAmout() - personSecurity);

        Record bank_record = Util.createBankRecord(origin);

        // entry.addAll(List.of(person_security, company_security, bank_record));
        entry.add(person_security);
        entry.add(company_security);
        entry.add(bank_record);

        return List.of(entry);
    }

    private List<AccountEntry> handleBankPayHousingProvidentFund(RawInfo origin,String companyName) {

        AccountEntry entry = new AccountEntry();

        Record record_dwPayable = Util.createRecord(origin);
        record_dwPayable.set借方金额(origin.getAmout() / 2);
        record_dwPayable.set科目代码("其他应付款-应交住房公积金（单位）");
        record_dwPayable.set科目代码(service.getAccountNumByMutilName("其他应付款-应交住房公积金（单位）", companyName));
        record_dwPayable.set摘要("缴纳住房公积金");

        Record record_grPayable = Util.createRecord(origin);
        record_grPayable.set借方金额(origin.getAmout() / 2);
        record_grPayable.set科目代码("其他应付款-应交住房公积金（个人）");
        record_grPayable.set科目代码(service.getAccountNumByMutilName("其他应付款-应交住房公积金（个人）", companyName));
        record_grPayable.set摘要("缴纳住房公积金");

        Record record_bank = Util.createBankRecord(origin);
        record_bank.set摘要("缴纳住房公积金");


        entry.add(record_dwPayable);
        entry.add(record_grPayable);
        entry.add(record_bank);

        return List.of(entry);
    }

    private List<AccountEntry> handleBankGroupItems(List<RawInfo> origins) {

        AccountEntry entry = new AccountEntry();

        if (origins.size() <= 0)
            return List.of();
        double sum = 0;
        String brief = "";

        boolean isIncome = false;
        for (RawInfo origin : origins) {
            if (origin.getBank_income() > 0.001) {
                isIncome = true;
            }
            if (origin.getType().equals(TypeEnum.Bank_Income_Receivable.value)) {
                brief = "收到货款";

            } else if (origin.getType().equals(TypeEnum.Bank_Pay_Payable.value)) {
                brief = "付货款";

            } else if (origin.getType().equals(TypeEnum.Bank_Pay_OtherPayable.value)) {
                brief = "其他款项";

            } else if (origin.getType().equals(TypeEnum.Bank_Income_OtherReceivable.value)) {
                brief = "其他款项";

            }

            Record record = Util.createRecord(origin);
            if (isIncome) {
                record.set贷方金额(origin.getBank_income());
            } else {
                record.set借方金额(origin.getBank_pay());
            }

            record.set科目名称(origin.getRelative_account());
            record.set科目代码(origin.getRelative_account_number());

            record.set摘要(brief.isBlank() ? origin.getBrief() : brief);

            sum += isIncome ? origin.getBank_income() : origin.getBank_pay();

            entry.add(record);
        }

        Record record_bank = Util.createBankRecord(origins.get(0));
        record_bank.set摘要(brief);
        if (isIncome) {
            record_bank.set借方金额(sum);
        } else {
            record_bank.set贷方金额(sum);
        }

        entry.add(record_bank);

        return List.of(entry);
    }

    private List<AccountEntry> handleBankTransferInternal(List<RawInfo> origins) {
        int max=origins.size();
        List<AccountEntry> entries=new ArrayList<>();
        String mark="Inter_Trans";

        for(int i=0;i<max;i++) {
            RawInfo one=origins.get(i);
            double amount=one.getBank_pay()>0.01?one.getBank_pay()*(-1):one.getBank_income(); //pay 是负数
            for(int j=i+1;j<max;j++) {
                RawInfo o=origins.get(j);
                if(o.getBankNum().equals(one.getRelative_account_number())&&!o.getBank_brief2().contains(mark)) {
                    double amount2=o.getBank_pay()>0?o.getBank_pay()*-1:o.getBank_income();

                    //必须是数值相同且不同借贷方向的才是重复的
                    if(Math.abs(-1*amount-amount2)<0.01) {
                        o.setBank_brief2(o.getBank_brief2()+"_"+mark); //添加一个标记，避免重复
                    }

                }
            }
        }


        origins.stream().filter(e->!e.getBank_brief2().contains(mark)).forEach(e->entries.add(createEntry(e)));

        return entries;
    }


    private AccountEntry createEntry(RawInfo origin) {

        AccountEntry entry = new AccountEntry();
        if (origin.getBank_income() > 0.01) { // 银行借方
            Record bank_record = Util.createBankRecord(origin);
            Record record = Util.createRecord(origin);
            record.set贷方金额(origin.getAmout());
            // entry.addAll(List.of(bank_record, record));
            entry.add(bank_record);
            entry.add(record);

        } else if (origin.getBank_pay() > 0.01) { // 银行贷方
            Record record = Util.createRecord(origin);
            record.set借方金额(origin.getAmout());
            Record bank_record = Util.createBankRecord(origin);
            // entry.addAll(List.of(record, bank_record));
            entry.add(bank_record);
            entry.add(record);
        }

        return entry;

    }
}

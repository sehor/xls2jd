package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.util.Util;
import com.pzr.xls2jd.core.domain.AccountPeriod;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-08-14:28
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class SalaryAndSecurityProcessor implements Processor {
private final ProcessorService service;
    public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {
        // TODO Auto-generated method stub


        return origins;
    }

    public List<AccountEntry> processToRecord(List<RawInfo> origins,String companyName) {
        
        AccountEntry entry_s=new AccountEntry();

        for (RawInfo origin : origins) {
            // System.out.println("salary: " + origin.getSalary_payable());

            AccountPeriod period = new AccountPeriod(origin.getOccur_date().plusMonths(1)); // 取下一个月的数据

            // 应付工资数额
            double payable_salary = service.findSalary(companyName, period.getBeginDay(),
                    period.getLastDay());
            if (payable_salary < 0.001) {
                payable_salary = origin.getBasicSalary();
            }
            double person_tax = service.findPersonTax(companyName, period.getBeginDay(),
                    period.getLastDay());
            double person_fund = service.findPersonFund(companyName, period.getBeginDay(),
                    period.getLastDay());

            String account="管理费用-工资费用";
            if(List.of("CBKJ").contains(companyName)) {
                account="车吧APP平台研发-费用性支出";
            }else if(List.of("AOKJ").contains(companyName)) {
                account="研发支出-费用性支出";
            }

            Record record_fee = Util.createRecord(origin);
            record_fee.set摘要("计提工资");
            record_fee.set借方金额(payable_salary + person_tax + origin.getPayedPersonSecurity() + person_fund); // 工资费用=应付工资+个税+个人社保+个人公积金
            record_fee.set科目名称(account);
            record_fee.set科目代码(service.getAccountNumByMutilName(account,companyName));

            entry_s.add(record_fee);

            // 如果有单位社保费计提社保
            if (origin.getPayedCompanySecurity() > 0.01) {
                Record record_comSecu = Util.createRecord(origin);
                record_comSecu.set摘要("计提社保费用");
                record_comSecu.set借方金额(origin.getPayedCompanySecurity());
                record_comSecu.set科目名称("社会保险费（单位）");
                record_comSecu.set科目代码(service.getAccountNumByMutilName("管理费用-社会保险费（单位）",companyName));
                
                Record record_comSecu1 = Util.createRecord(origin);
                record_comSecu1.set摘要("计提社保费");
                record_comSecu1.set贷方金额(origin.getPayedCompanySecurity());
                record_comSecu1.set科目名称("应交社会保险费（单位）");
                record_comSecu1.set科目代码(service.getAccountNumByMutilName("其他应付款-应交社会保险费（单位）",companyName));


                entry_s.add(record_comSecu);
                entry_s.add(record_comSecu1);
            }

            // 如果有个人社保费计提社保
            if (origin.getPayedPersonSecurity() > 0.01) {
                Record record_perSecu = Util.createRecord(origin);
                record_perSecu.set摘要("计提社保");
                record_perSecu.set贷方金额(origin.getPayedPersonSecurity());
                record_perSecu.set科目名称("应交社会保险费（个人）");
                record_perSecu.set科目代码(getAccountNum("其他应付款-应交社会保险费（个人）",companyName));
                entry_s.add(record_perSecu);
            }

            // 如果有个人所得税计提个人所得税
            if (person_tax > 0.01) {
                Record record_perTax = Util.createRecord(origin);
                record_perTax.set摘要("计提个人所得税");
                record_perTax.set贷方金额(person_tax);
                record_perTax.set科目名称("应交个人所得税");
                record_perTax.set科目代码(getAccountNum("应交税费-应交个人所得税",companyName));
                entry_s.add(record_perTax);
            }

            // 如果有公积金计提公积金
            if (person_fund > 0.01) {
                Record record_fund = Util.createRecord(origin);
                record_fund.set摘要("计提公积金");
                record_fund.set借方金额(person_fund);// 单位等于个人
                record_fund.set科目名称("住房公积金费用（单位）");
                record_fund.set科目代码(getAccountNum("管理费用-住房公积金费用（单位）",companyName));

                Record record_fund1 = Util.createRecord(origin);
                record_fund1.set摘要("计提公积金");
                record_fund1.set贷方金额(person_fund); // 单位等于个人
                record_fund1.set科目名称("应交住房公积金（个人）");
                record_fund1.set科目代码(getAccountNum("其他应付款-应交住房公积金（个人）",companyName));

                Record record_fund2 = Util.createRecord(origin);
                record_fund2.set摘要("计提公积金");
                record_fund2.set贷方金额(person_fund); // 单位等于
                record_fund2.set科目名称("应交住房公积金（单位）");
                record_fund2.set科目代码(getAccountNum("其他应付款-应交住房公积金（单位）",companyName));

                entry_s.add(record_fund);
                entry_s.add(record_fund1);
                entry_s.add(record_fund2);
            }

            // 应付工资
            Record record_salaryPayable = Util.createRecord(origin);
            record_salaryPayable.set摘要("计提工资");
            record_salaryPayable.set贷方金额(payable_salary);// 单位和个人平分
            record_salaryPayable.set科目名称("应付工资");
            record_salaryPayable.set科目代码(getAccountNum("应付职工薪酬-应付工资",companyName));

            entry_s.add(record_salaryPayable);
        }

        return List.of(entry_s);

    }

    private String getAccountNum(String accountName,String companyName) {

        return service.getAccountNumByMutilName(accountName, companyName);
    }
}

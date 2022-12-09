package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-08-14:39
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class VATProcessor implements Processor{

    private final ProcessorService service;

    public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {
        // TODO Auto-generated method stub

        return origins;
    }

    public List<AccountEntry> processToRecord(List<RawInfo> origins,String companyName) {

        AccountEntry entry_vat=new AccountEntry();
        List<Record> records = new ArrayList<>();
        if (origins.size() <= 0)
            return List.of();


        for (RawInfo origin : origins) {

            for (int i = 0; i <= 5; i++) {
                Record record = new Record();
                record.set日期(origin.getOccur_date());

                record.set摘要("月末增值税处理");
                records.add(record);
                entry_vat.add(record);
            }

            records.get(0).set贷方金额(origin.getAmout());// 未交增值税
            records.get(0).set科目名称("应交税费-未交增值税");
            records.get(0).set科目代码(service.getAccountNumByMutilName("应交税费-未交增值税",companyName));

            records.get(1).set借方金额(origin.getAmout());// 转出未交增值税
            records.get(1).set科目名称("应交增值税-转出未交增值税");
            records.get(1).set科目代码(service.getAccountNumByMutilName("应交税费-应交增值税-转出未交增值税",companyName));

            float CJ_Tax_Rate = 0.07f;
            records.get(3).set贷方金额(origin.getAmout() * CJ_Tax_Rate);// 城建税
            records.get(3).set科目名称("应交税费-应交城市维护建设税");
            records.get(3).set科目代码(service.getAccountNumByMutilName("应交税费-应交城市维护建设税",companyName));

            float JY_Tax_Rate = 0.03f;
            records.get(4).set贷方金额(origin.getAmout() * JY_Tax_Rate);// 教育费
            records.get(4).set科目名称("应交税费-教育费附加");
            records.get(4).set科目代码(service.getAccountNumByMutilName("应交税费-教育费附加",companyName));

            float DFJY_Tax_Rate = 0.02f;
            records.get(5).set贷方金额(origin.getAmout() * DFJY_Tax_Rate);// 地方教育费
            records.get(5).set科目名称("应交税费-地方教育费附加");
            records.get(5).set科目代码(service.getAccountNumByMutilName("应交税费-地方教育费附加",companyName));

            records.get(2).set借方金额(records.get(3).get贷方金额() + records.get(4).get贷方金额() + records.get(5).get贷方金额()); // 税金及附加
            records.get(2).set科目名称("营业税金及附加-增值税税金及附加");
            records.get(2).set科目代码(service.getAccountNumByMutilName("营业税金及附加-增值税税金及附加",companyName));

        }

        return List.of(entry_vat);
    }

}

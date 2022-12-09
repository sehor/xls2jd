package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.util.Util;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class IssueInvoiceProcessor  implements Processor{

	private final ProcessorService service;

	public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {
		// TODO Auto-generated method stub
		setRelateAccountNum(origins, companyName);
		Util.mergeSameClassification(origins);
		return origins;
	}

	public List<AccountEntry> processToRecord(List<RawInfo> origins,String companyName) {
		// TODO Auto-generated method stub

		AccountEntry entry_issue=new AccountEntry();
		if (origins.size() < 1)
			return List.of();

		LocalDate date=origins.get(0).getOccur_date();
		Map<String, Double> map = new HashMap<>();// 用来计算不同收入种类的收入
		double tax_sum = 0;
		for (RawInfo origin : origins) {
			Record record_receivable = Util.createRecord(origin);
			record_receivable.set摘要("开出发票，确认应收款项");
			record_receivable.set借方金额(origin.getInvoice_amount() + origin.getInvoice_tax());
			record_receivable.set科目名称(origin.getRelative_account());
			record_receivable
					.set科目代码(origin.getRelative_account_number());

			tax_sum += origin.getInvoice_tax();

			double income_sum = (map.get(origin.getIncome_account()) != null ? map.get(origin.getIncome_account()) : 0);
			map.put(origin.getIncome_account(), origin.getInvoice_amount() + income_sum);
			record_receivable.set日期(date);

			entry_issue.add(record_receivable);
		}

		// 写入各种收入科目
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			Record record_income = Util.createRecord(origins.get(0));
			record_income.set摘要("开出发票，确认收入");
			record_income.set科目名称(entry.getKey());
			record_income.set科目代码(service.getAccountNumByMutilName(entry.getKey(), companyName));
			record_income.set贷方金额(entry.getValue());

			record_income.set日期(date);
			entry_issue.add(record_income);
		}

		// 写入增值税-销项

		Record record_tax = Util.createRecord(origins.get(0));
		record_tax.set摘要("开出发票，计提税费");
		record_tax.set科目名称("应交税费-应交增值税-销项税额");
		record_tax.set科目代码(service.getAccountNumByMutilName("应交税费-应交增值税-销项税额",companyName));
		record_tax.set贷方金额(tax_sum);
		record_tax.set日期(date);

		entry_issue.add(record_tax);

		return List.of(entry_issue);
	}
	
	
	
	private void setRelateAccountNum(List<RawInfo> origins,String companyName) {
		
		 for(RawInfo origin:origins) {
			 String num=service.getAccountNumByMutilName("应收账款-"+origin.getRelative_account(), companyName);
			 origin.setRelative_account_number(num); 
		 }
	}


}

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
public class ReceiveInvoiceProcessor implements Processor{

	private final ProcessorService service;
	public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {

		setRelateAccountNum(origins, companyName);
		Util.mergeSameClassification(origins);
		return origins;
	}

	@Override
	public List<AccountEntry> processToRecord(List<RawInfo> rawInfos, String companyName) {

		AccountEntry entry_receive=new AccountEntry();
		if (rawInfos.size() < 1)
			return List.of();

		LocalDate date=rawInfos.get(0).getOccur_date();
		Map<String, Double> map = new HashMap<>();// 用来计算不同种类的成本费用
		double tax_sum = 0;
		for (RawInfo origin : rawInfos) {
			Record record_payable = Util.createRecord(origin);
			record_payable.set摘要("收到发票，确认应付款项");
			record_payable.set贷方金额(origin.getInvoice_amount() + origin.getInvoice_tax());
			record_payable.set科目名称(origin.getRelative_account());
			record_payable
					.set科目代码(origin.getRelative_account_number());

			tax_sum += origin.getInvoice_tax();

			double pay_sum = (map.get(origin.getPayable_account()) != null ? map.get(origin.getPayable_account()) : 0);
			map.put(origin.getPayable_account(), origin.getInvoice_amount() + pay_sum);
			record_payable.set日期(date);

			entry_receive.add(record_payable);
		}

		// 写入各种成本费用科目
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			Record record_income = Util.createRecord(rawInfos.get(0));
			record_income.set摘要("收到发票，确认成本费用");
			record_income.set科目名称(entry.getKey());
			record_income.set科目代码(service.getAccountNumByMutilName(entry.getKey(),companyName));
			record_income.set借方金额(entry.getValue());

			record_income.set日期(date);
			entry_receive.add(record_income);
		}

		// 写入增值税-进项

		Record record_tax = Util.createRecord(rawInfos.get(0));
		record_tax.set摘要("收到发票，确认进项税");
		record_tax.set科目名称("应交增值税-进项税额");

		record_tax.set科目代码(service.getAccountNumByMutilName("应交税费-应交增值税-进项税额",companyName));
		record_tax.set借方金额(tax_sum);
		record_tax.set日期(date);

		entry_receive.add(record_tax);

		return List.of(entry_receive);

	}


	private void setRelateAccountNum(List<RawInfo> origins,String companyName) {
		
		String num;
		 for(RawInfo origin:origins) {
			 if(origin.getRelative_account().contains("国家税局")) {
				 
				 num=service.getAccountNumByMutilName("应付账款-国家税务局代开发票的供应商",companyName );
				 origin.setRelative_account_number(num);
				 continue;
			 }
			 
			 num=service.getAccountNumByMutilName("应付账款-"+origin.getRelative_account(), companyName);
			 num=num.equals("未找到")?service.getAccountNumByMutilName("其他应付款-"+origin.getRelative_account(), companyName):num;
			 num=num.equals("未找到")?service.getAccountNumByMutilName("其他应付款-其他", companyName):num;
			 origin.setRelative_account_number(num); 
		 }
	}
	


}

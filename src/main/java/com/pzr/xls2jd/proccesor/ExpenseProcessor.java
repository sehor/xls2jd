package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.util.Util;
import com.pzr.xls2jd.core.domain.TypeEnum;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseProcessor implements Processor {

	private final ProcessorService service;

	

	public List<RawInfo> preProcess(List<RawInfo> rawInfos,String companyName) {
		// TODO Auto-generated method stub
	
		List<RawInfo> list= Util.filterAndCopy(rawInfos, e->e.getType().contains(TypeEnum.Expense.value));
		for(RawInfo rawInfo:list) {
			rawInfo.setRelative_account_number(service.getAccountNumByMutilName(rawInfo.getRelative_account(), companyName));
		}
		
		return list;
	}

	public List<AccountEntry> processToRecord(List<RawInfo> rawInfos,String companyName) {

		if (rawInfos.size() <= 0)
			return List.of();

		AccountEntry entry = new AccountEntry();

		double sum_mount = 0, sum_tax = 0;

		List<Record> expenseRecords = new ArrayList<>();
		for (RawInfo rawInfo : rawInfos) {
			sum_mount += rawInfo.getInvoice_amount();
			sum_tax += rawInfo.getInvoice_tax();
			Record record = Util.createRecord(rawInfo);
			record.set借方金额(rawInfo.getInvoice_amount());
			expenseRecords.add(record);
		}

		mergeRecords(expenseRecords).forEach(entry::add);

		if (sum_tax > 0.01) {
			Record record_tax = Util.createRecord(rawInfos.get(0));
			record_tax.set摘要("报销费用,税费");
			record_tax.set借方金额(sum_tax);
			record_tax.set科目名称("进项税额");
			record_tax.set科目代码(service.getAccountNumByMutilName("应交税费-应交增值税-进项税额", companyName));
			entry.add(record_tax);
		}

		Record record_present = Util.createRecord(rawInfos.get(0));
		record_present.set摘要("报销费用，垫付");
		record_present.set贷方金额(sum_mount + sum_tax);

		String chargePerson = service.findRepresentiveOfCompany(companyName);
		record_present.set科目名称(chargePerson);
		record_present.set科目代码(service.getAccountNumByMutilName("其他应付款-" + chargePerson, companyName));

		entry.add(record_present);

		return List.of(entry);
	}

	private List<Record> mergeRecords(List<Record> records) {
		// map<科目代码，累计数>
		Map<String, Double> map = new HashMap<>();
		List<Record> records_merge = new ArrayList<>();
		for (Record record : records) {
			Double amount = map.get(record.get科目代码());
			double d = record.get借方金额() != null ? record.get借方金额() : record.get贷方金额();
			if (amount != null) {
				amount = amount + d;
				map.put(record.get科目代码(), amount);
			} else {
				map.put(record.get科目代码(), d);
			}
		}

		for (Map.Entry<String, Double> entry : map.entrySet()) {

			for (Record record : records) {

				if (record.get科目代码().equals(entry.getKey())) {
					if (record.get借方金额() != null) {
						record.set借方金额(entry.getValue());
					} else {
						record.set贷方金额(entry.getValue());
					}
					records_merge.add(record);
					break;
				}
			}
		}

		return records_merge;
	}


}

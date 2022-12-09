package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.util.Util;
import com.pzr.xls2jd.core.domain.TypeEnum;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillProcessor implements Processor {

	private final ProcessorService service;


	public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {
		// TODO Auto-generated method stub
		
		
		for (RawInfo origin : origins) {
			if (!origin.getType().equalsIgnoreCase(TypeEnum.Bill.value)) {
				continue;
			}
			if (origin.getBill_income() > 0.01) {
				origin.setType(TypeEnum.Bill_Income.value);

				String accountNumber = service.getAccountNumByMutilName("应收账款-" + origin.getRelative_account(),
						companyName);
				accountNumber = accountNumber.equals("未找到")
						? service.getAccountNumByMutilName("应付账款-" + origin.getRelative_account(),
								companyName)
						: accountNumber;
				origin.setRelative_account_number(accountNumber);

				origin.setBrief("收到汇票");
			} else {
				origin.setType(TypeEnum.Bill_Pay.value);

				String accountNumber = service.getAccountNumByMutilName("应付账款-" + origin.getRelative_account(),
						companyName);
				accountNumber = accountNumber.equals("未找到")
						? service.getAccountNumByMutilName("应收账款-" + origin.getRelative_account(),
								companyName)
						: accountNumber;
				origin.setRelative_account_number(accountNumber);

				origin.setBrief("发出汇票");
			}

		}

		return origins;
	}


	public List<AccountEntry> processToRecord(List<RawInfo> origins,String companyName) {

		AccountEntry entry_pay=new AccountEntry();
		AccountEntry entry_income=new AccountEntry();

		if (origins.size() <= 0)
			return List.of();

		LocalDate date=origins.get(0).getOccur_date();
		double bill_creceivable = 0, bill_paybale = 0;
		String billAccountNum = service.getAccountNumByMutilName("应收票据",companyName);
		for (RawInfo origin : origins) {
			// 全部是收到汇票，没有涉及到自身发起汇票，所以全是应收票据
			if (origin.getType().equals(TypeEnum.Bill_Income.value)) {

				bill_creceivable += origin.getBill_income();
				// 应收账款
				Record record_receive = Util.createRecord(origin);
				record_receive.set贷方金额(origin.getBill_income());


				entry_income.add(record_receive);
			} else {
				bill_paybale += origin.getBill_pay();
				// 应付账款
				Record record_pay = Util.createRecord(origin);
				record_pay.set借方金额(origin.getBill_pay());


				entry_pay.add(record_pay);
			}

		}

		if (bill_creceivable > 0) {

			RawInfo origin_bill_income = new RawInfo();
			origin_bill_income.setType(TypeEnum.Bill_Income.value);

			// 收到汇票
			Record record_billIncome = Util.createRecord(origin_bill_income);
			record_billIncome.set借方金额(bill_creceivable);
			record_billIncome.set科目代码(billAccountNum);
			record_billIncome.set科目名称("应收票据");
			record_billIncome.set日期(date);
			record_billIncome.set摘要("收到汇票");

			entry_income.add(record_billIncome);

		}

		if (bill_paybale > 0) {

			RawInfo origin_bill_issue = new RawInfo();
			origin_bill_issue.setType(TypeEnum.Bill_Pay.value);
			// 背书转发汇票
			Record record_billPay = Util.createRecord(origin_bill_issue);
			record_billPay.set贷方金额(bill_paybale);
			record_billPay.set科目代码(billAccountNum);
			record_billPay.set科目名称("应收票据");
			record_billPay.set日期(date);
			record_billPay.set摘要("发出汇票");

			entry_pay.add(record_billPay);
		}



		return List.of(entry_income,entry_pay);
	}


}

package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.util.Util;
import com.pzr.xls2jd.core.domain.AccountPeriod;
import com.pzr.xls2jd.core.domain.TypeEnum;
import com.pzr.xls2jd.core.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoldToCostProcessor implements Processor{
  private final ProcessorService service;

	

	public List<RawInfo> preProcess(List<RawInfo> origins,String companyName) {

		return Util.filter(origins, e->e.getType().contains(TypeEnum.Issue_Invoice.value));
	}

	public List<AccountEntry> processToRecord(List<RawInfo> origins, String companyName) {

		if (origins.size() <= 0)
			return List.of();
		LocalDate date = new AccountPeriod(origins.get(0).getOccur_date()).getLastDay();
		AccountEntry entry = new AccountEntry();

		//simple version , just considered main accounts 
		double sum=0;
		for(RawInfo origin:origins) {
			sum+=origin.getInvoice_amount();
		}
		Record record_cost = new Record();
		record_cost.set摘要("结转成本");
		record_cost.set日期(date);
		record_cost.set借方金额(sum*service.grossProfitRate(companyName));
		record_cost.set科目名称("主营业务成本");
		record_cost.set科目代码(
				service.mainCostAccountNum(companyName));

		entry.add(record_cost);

		Record record_relate_cost=new Record();
		record_relate_cost.set摘要("结转成本");
		record_relate_cost.set日期(date);
		record_relate_cost.set贷方金额(record_cost.get借方金额());
		record_relate_cost.set科目名称("库存商品");
		record_relate_cost.set科目代码(
				service.mainInvotoryAccounNum(companyName));

		entry.add(record_relate_cost);

		return List.of(entry);
	}

}

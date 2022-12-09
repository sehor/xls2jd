package com.pzr.xls2jd.core.util;

import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;




public class Util {

	public static List<RawInfo> filterAndCopy(List<RawInfo> origins, Predicate<RawInfo> predicate) {

		return filter(copyOrigins(origins), predicate);
	}

	public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {

		return list.stream().filter(predicate).collect(Collectors.toList());
	}

	public static List<RawInfo> copyOrigins(List<RawInfo> list) {

		List<RawInfo> newList = new ArrayList<>();
		list.forEach(e -> {
			newList.add(e.clone());
		});

		return newList;
	}
	
	public static Record createRecord(RawInfo origin) {
		Record record=new Record();
		record.set摘要(origin.getBrief());
		record.set日期(origin.getOccur_date());
		record.set科目名称(origin.getRelative_account());
		record.set科目代码(origin.getRelative_account_number()==null?"未找到":origin.getRelative_account_number()); 
		return record;
	}
	
	public static Record createBankRecord(RawInfo origin) {
		
		Record record=createRecord(origin);
	  
		if (origin.getBank_income() > 0.01) {
			record.set借方金额(origin.getAmout());
		} else if (origin.getBank_pay() > 0.01) {
			record.set贷方金额(origin.getAmout());
		}

			
		record.set科目代码(origin.getBankNum());
		record.set科目名称("银行存款"+"_"+origin.getBankNum());
		
		return record;
	}

	public static void mergeSameClassification(List<RawInfo> origins) {
		Map<String,RawInfo> map=new HashMap<>();
		for(RawInfo origin:origins) {
			RawInfo mergeOrigin=map.get(origin.getRelative_account()); //以relative account为key merge
			if(mergeOrigin==null) {
				map.put(origin.getRelative_account(), origin);
			}else {

				mergeOrigin.setInvoice_amount(mergeOrigin.getInvoice_amount()+origin.getInvoice_amount());
				mergeOrigin.setInvoice_tax(mergeOrigin.getInvoice_tax()+origin.getInvoice_tax());
				mergeOrigin.setOccur_date(origin.getOccur_date().isAfter(mergeOrigin.getOccur_date())?origin.getOccur_date():mergeOrigin.getOccur_date()); //选最晚的date
			}
		}
		origins.clear();
		origins.addAll(map.values());
	}

}

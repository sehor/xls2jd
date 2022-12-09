package com.pzr.xls2jd.core.service;

import com.pzr.xls2jd.core.domain.RawInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public interface ProcessorService {

	String getCompanyCnName(String name);
	String getAccountNumByMutilName(String mutilAccountName,String companyName);
	boolean isContainKeyWord(String text,String keyWord);
	boolean needToBeMerged(RawInfo origin);
	String mergeMark(RawInfo origin);
	double findPersonSecurity(String companyName, LocalDate beginDay, LocalDate lastDay);
	double findSalary(String companyName, LocalDate beginDay, LocalDate lastDay);
	double findPersonTax(String companyName, LocalDate beginDay, LocalDate lastDay);
	double findPersonFund(String companyName, LocalDate beginDay, LocalDate lastDay);
	String findRepresentiveOfCompany(String companyName);
	String mainCostAccountNum(String companyName);
	String mainInvotoryAccounNum(String companyName);
	double grossProfitRate(String companyName);
	Map<String,String> getBankMap();
}

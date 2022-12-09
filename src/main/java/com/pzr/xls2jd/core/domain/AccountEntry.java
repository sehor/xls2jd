package com.pzr.xls2jd.core.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountEntry {
   
	private List<Record> records=new ArrayList<>();
	private final  int Max_Brief_Length=20;

	public void add(Record record) {
		records.add(record);
	}
	
	public void addAll(List<Record> records) {
		records.addAll(records);
	}
	
	public List<Record> records() {
		
		return this.records;
	}
	

	
	public void tidy() {
		
		//排序，贷方在后面
		this.records.sort((r1,r2)->{
			 if(r1.get贷方金额()!=null&&r2.get借方金额()!=null) return 1;
			 if(r1.get借方金额()!=null&&r2.get贷方金额()!=null) return -1;
			 return 0;
		});
		
		//四舍五入两位小数
		//设置最长摘要长度
		this.records.forEach(e->{
			/*
			 * if(e.get借方金额()!= null) { e.set借方金额(new BigDecimal(e.get借方金额()).setScale(2,
			 * RoundingMode.HALF_UP).doubleValue()); }else if(e.get贷方金额()!=null) {
			 * e.set贷方金额(new BigDecimal(e.get贷方金额()).setScale(2,
			 * RoundingMode.HALF_UP).doubleValue()); }
			 */
			
			e.set原币金额(e.get借方金额()!=null?e.get借方金额():e.get贷方金额());
			
			if(e.get摘要().length()>Max_Brief_Length) {
				e.set摘要(e.get摘要().substring(0, Max_Brief_Length));
			}
			
		});
		
		//全部设置成最晚的日期
		//设置凭证分录号
	    LocalDate lastDate=LocalDate.of(1980, 1, 1);
		int recordNum=1;
		for(Record record:this.records) {
			
			if(record.get日期().isAfter(lastDate)) {
				lastDate=record.get日期();
			}	
		}
		
		for(Record record:this.records) {
			record.set日期(lastDate);
			record.set分录序号(recordNum++);
		}
		
	}
}

package com.pzr.xls2jd.excelTool;

public enum ExcelType {

	XLS("xls"), XLSX("xlsx");
	public final String suffix;

	private ExcelType(String suffix) {
		this.suffix = suffix;
	}
}

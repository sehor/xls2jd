package com.pzr.xls2jd.core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Record {

		private LocalDate 日期;
		private String 凭证字;
		private Integer 凭证号;
		private Integer 附件数;
		private Integer 分录序号;

		private String 摘要="";
		private String 科目代码;
		private String 科目名称;
		private Double 借方金额;
		private Double 贷方金额;
		
		private String 客户;
		private String 供应商;
		private String 职员;
		private String 项目;
		private String 部门;
		private String 存货;

		private Boolean 是否限定;
		private String 自定义辅助核算类别;
		private String 自定义辅助核算编码;
		
		private Double 数量;
		private Double 单价;
		private Double 原币金额;
		private String 币别="RMB";
		private Double 汇率=Double.valueOf(1);
		
		
		public LocalDate get日期() {
			return 日期;
		}
		public void set日期(LocalDate 日期) {
			this.日期 = 日期;
		}
		public String get凭证字() {
			return 凭证字;
		}
		public void set凭证字(String 凭证字) {
			this.凭证字 = 凭证字;
		}
		public Integer get凭证号() {
			return 凭证号;
		}
		public void set凭证号(Integer 凭证号) {
			this.凭证号 = 凭证号;
		}
		public Integer get附件数() {
			return 附件数;
		}
		public void set附件数(Integer 附件数) {
			this.附件数 = 附件数;
		}
		public Integer get分录序号() {
			return 分录序号;
		}
		public void set分录序号(Integer 分录序号) {
			this.分录序号 = 分录序号;
		}
		public String get摘要() {
			return 摘要;
		}
		public void set摘要(String 摘要) {
			this.摘要 = 摘要;
		}
		public String get科目代码() {
			return 科目代码;
		}
		public void set科目代码(String 科目代码) {
			this.科目代码 = 科目代码;
		}
		public String get科目名称() {
			return 科目名称;
		}
		public void set科目名称(String 科目名称) {
			this.科目名称 = 科目名称;
		}
		public Double get借方金额() {
			return 借方金额;
		}
		public void set借方金额(Double 借方金额) {
			
			this.借方金额 = new BigDecimal(借方金额).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		public Double get贷方金额() {
			return 贷方金额;
		}
		public void set贷方金额(Double 贷方金额) {
			this.贷方金额 = new BigDecimal(贷方金额).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		public String get客户() {
			return 客户;
		}
		public void set客户(String 客户) {
			this.客户 = 客户;
		}
		public String get供应商() {
			return 供应商;
		}
		public void set供应商(String 供应商) {
			this.供应商 = 供应商;
		}
		public String get职员() {
			return 职员;
		}
		public void set职员(String 职员) {
			this.职员 = 职员;
		}
		public String get项目() {
			return 项目;
		}
		public void set项目(String 项目) {
			this.项目 = 项目;
		}
		public String get部门() {
			return 部门;
		}
		public void set部门(String 部门) {
			this.部门 = 部门;
		}
		public String get存货() {
			return 存货;
		}
		public void set存货(String 存货) {
			this.存货 = 存货;
		}
		public Boolean get是否限定() {
			return 是否限定;
		}
		public void set是否限定(Boolean 是否限定) {
			this.是否限定 = 是否限定;
		}
		public String get自定义辅助核算类别() {
			return 自定义辅助核算类别;
		}
		public void set自定义辅助核算类别(String 自定义辅助核算类别) {
			this.自定义辅助核算类别 = 自定义辅助核算类别;
		}
		public String get自定义辅助核算编码() {
			return 自定义辅助核算编码;
		}
		public void set自定义辅助核算编码(String 自定义辅助核算编码) {
			this.自定义辅助核算编码 = 自定义辅助核算编码;
		}
		public Double get数量() {
			return 数量;
		}
		public void set数量(Double 数量) {
			this.数量 = 数量;
		}
		public Double get单价() {
			return 单价;
		}
		public void set单价(Double 单价) {
			this.单价 = 单价;
		}
		public Double get原币金额() {
			return 原币金额;
		}
		public void set原币金额(Double 原币金额) {
			this.原币金额 = 原币金额;
		}
		public String get币别() {
			return 币别;
		}
		public void set币别(String 币别) {
			this.币别 = 币别;
		}
		public Double get汇率() {
			return 汇率;
		}
		public void set汇率(Double 汇率) {
			this.汇率 = 汇率;
		}
		@Override
		public String toString() {
			return "Record [日期=" + 日期 + ", 凭证字=" + 凭证字 + ", 凭证号=" + 凭证号 + ", 附件数=" + 附件数 + ", 分录序号=" + 分录序号 + ", 摘要="
					+ 摘要 + ", 科目代码=" + 科目代码 + ", 科目名称=" + 科目名称 + ", 借方金额=" + 借方金额 + ", 贷方金额=" + 贷方金额 + "]";
		}
		
	
		
}

package com.pzr.xls2jd.core.domain;



import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Classification {

	private String id;

	private String 编码;

	private String 名称;


	private String 类别;

	private String 余额方向;
	
	private String companyName;
	
	private String mutilName;

}
package com.pzr.xls2jd.excelTool;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @Author pzr
 * @date:2022-12-04-11:33
 * @Description:
 **/
public class Writer {



    public static  <T> void writeToFile(List<T> beans, File file,Class clzz,boolean strip) {

        ExcelType type = (file.getName().toLowerCase().endsWith("xls")) ? ExcelType.XLS : ExcelType.XLSX;

        Workbook workbook = (type==ExcelType.XLSX)?new XSSFWorkbook():new HSSFWorkbook();;
        Sheet sheet = workbook.createSheet("sheet0");
        Field[] fields = ExcelUtil.getFullFields(clzz);

        Row headRow = sheet.createRow(0);
        for (int i = 0; i < fields.length; i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(fields[i].getName());
        }

        int rowIndex=1;
        for (T t : beans) {
            Row row = sheet.createRow(rowIndex);
            for (int i=0;i<fields.length;i++) {
                Cell cell = row.createCell(i);
                try {
                    ExcelUtil.writeFieldValueToCell(t, fields[i], cell);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            rowIndex++;

        }
        if (strip) {
            ExcelUtil.stripe(workbook,0,1);
        }

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            workbook.write(output);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                assert output != null;
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

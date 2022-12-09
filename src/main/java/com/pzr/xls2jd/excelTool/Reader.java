package com.pzr.xls2jd.excelTool;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author pzr
 * @date:2022-12-04-9:55
 * @Description:
 **/
public class Reader {

    public static <T> List<T> readFromExcel(Class clzz,File file, String sheetName, int headRow, int starRow)  {
         InputStream input = null;
         Workbook workbook = null;
         Field[] fields= ExcelUtil.getFullFields(clzz);
         List<T> ans = new ArrayList<>();

         try {
              input = new FileInputStream(file);
              workbook = file.getName().toLowerCase().endsWith("xlsx") ? new XSSFWorkbook(input)
                     : new HSSFWorkbook(input);
             Sheet sheet=workbook.getSheet(sheetName);
             Row titleRow=sheet.getRow(headRow);

             Map<Integer, Field> map = new HashMap<>();
             for (int i = 0; i <= titleRow.getLastCellNum(); i++) {
                 String value=ExcelUtil.getCellValueByCell(titleRow.getCell(i));
                 for (Field field : fields) {
                     if(field.getName().equals(value)){
                         map.put(i,field);
                     }
                 }
             }

             for (int i = starRow; i <=sheet.getLastRowNum() ; i++) {
                 Row row = sheet.getRow(i);
                 if(row==null){
                     continue;
                 }
                 T t =(T) clzz.getDeclaredConstructor().newInstance();
                 for (Map.Entry<Integer, Field> entry : map.entrySet()) {
                     Cell cell = row.getCell(entry.getKey());
                     if (cell != null) {

                         String cellValue = ExcelUtil.getCellValueByCell(cell);
                         ExcelUtil.setFieldsValue(t, entry.getValue(), cellValue);
                     }
                 }
                 ans.add(t);
             }

         }catch (Exception e){
             e.printStackTrace();
         }finally {
             if (workbook != null) {
                 try {
                     workbook.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             if (input != null) {
                 try {
                     input.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }

         return ans;
     }


     private static Map<Field, Integer> creatMap(Field[] fields,List<String> sheetHeads){
         Map<String, Integer> headsMap = new HashMap<>();
         for (int i = 0; i < sheetHeads.size(); i++) {
              headsMap.put(sheetHeads.get(i),i);
         }
         Map<Field, Integer> map = new HashMap<>();

         for (Field field : fields) {
             if (headsMap.containsKey(field.getName())) {
                 map.put(field, headsMap.get(field.getName()));
                 System.out.println("head name: " + field.getName() + " is match.");
             }
         }
         return map;
     }





}

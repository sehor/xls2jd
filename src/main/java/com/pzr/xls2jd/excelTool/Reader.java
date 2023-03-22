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

    public static <T> List<T> readFromExcel(Class clzz,InputStream input, String sheetName, int headRow, int starRow,String type)  {

         Workbook workbook = null;
         Field[] fields= ExcelUtil.getFullFields(clzz);
         List<T> ans = new ArrayList<>();

         try {

              workbook = type.toLowerCase().endsWith("xlsx") ? new XSSFWorkbook(input)
                     : new HSSFWorkbook(input);
             Sheet sheet=workbook.getSheet(sheetName);
             if(sheet==null){
                 for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                     if (workbook.getSheetAt(i).getSheetName().contains(sheetName)) {
                         sheet=workbook.getSheetAt(i);
                         break;
                     }
                 }
             }
             if(sheet==null){
                 sheet = workbook.getSheetAt(0);
             }
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
             //System.out.println(sheet.getLastRowNum());
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
                         ExcelUtil.setFieldValue(t, entry.getValue(), cellValue);
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

    public static <T> List<T> readFromExcel(Class clzz,File file, String sheetName, int headRow, int starRow)  {
        try {
            String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            return readFromExcel(clzz,new FileInputStream(file),sheetName,headRow,starRow,suffix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



     private static Map<Integer, Field> creatMap(Field[] fields,List<String> sheetHeads,Map<String,String> fieldsToSheetTitle){
         HashMap<Integer, Field> map = new HashMap<>();
         for (Field field : fields) {
             int index = sheetHeads.indexOf(field.getName());
             if (index > -1) {
                 map.put(index, field);
             }
         }
         return map;
     }


    public static void main(String[] args) {

    }


}

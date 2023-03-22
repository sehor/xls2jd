package com.pzr.xls2jd.excelTool;

import org.apache.poi.ss.usermodel.*;
import org.apache.xmlbeans.impl.regex.ParseException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author pzr
 * @date:2022-12-04-6:43
 * @Description:
 **/
public class ExcelUtil {

    public static String getCellValueByCell(Cell cell) {
        // 判断是否为null或空串
        if (cell == null || cell.toString().trim().equals("")) {
            return "";
        }
        String cellValue = "";
        CellType cellType = cell.getCellType();

        switch (cellType) {
            case NUMERIC: // 数字
                short format = cell.getCellStyle().getDataFormat();
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = null;
                    // System.out.println("cell.getCellStyle().getDataFormat()="+cell.getCellStyle().getDataFormat());
                    if (format == 20 || format == 32) {
                        sdf = new SimpleDateFormat("HH:mm");
                    } else if (format == 14 || format == 31 || format == 57 || format == 58) {
                        // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                    } else {// 日期
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }
                    try {
                        cellValue = sdf.format(cell.getDateCellValue());// 日期
                    } catch (Exception e) {
                        try {
                            throw new Exception("exception on get date data !".concat(e.toString()));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    } finally {
                        sdf = null;
                    }
                } else {
                    BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
                    cellValue = bd.toPlainString();// 数值 这种用BigDecimal包装再获取plainString，可以防止获取到科学计数值
                }
                break;
            case STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                ;
                break;
            case FORMULA: // 公式
                cellValue = switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC -> cell.getNumericCellValue() + "";
                    case STRING -> cell.getStringCellValue();
                    case BOOLEAN -> cell.getBooleanCellValue() + "";
                    default -> "";
                };
                break;
            case BLANK: // 空值
                cellValue = "";
                break;
            case ERROR: // 故障
                cellValue = "ERROR VALUE";
                break;
            default:
                cellValue = "UNKNOWN VALUE";
                break;
        }
        return cellValue;
    }

    public static void setFieldValue(Object t, Field field, String value) {
        ExcelUtil.setFieldValue(t, field, value, 2);
    }


    public static void setFieldValue(Object t, Field field, String value, int digit) {

        field.setAccessible(true);
        //System.out.println("set field value: "+value);
        try {
            if (field.getType() == String.class)
                field.set(t, value);
            else if (field.getType() == Date.class) {

                field.set(t, new SimpleDateFormat("yyyy-MM-dd").parse(value));

            }
            else if (field.getType() == Integer.class || field.getType() == int.class) {
                if (value.isEmpty()) {
                    field.set(t, 0);
                } else {
                    field.set(t, (int) Math.rint(Double.parseDouble(value)));
                }
                ; // 有小数点的字符串，四舍五入
            }
            else if (field.getType() == Float.class || field.getType() == float.class) {
                if (value.isEmpty()) {
                    field.set(t, 0.00F);
                } else
                    field.set(t, new BigDecimal(value).setScale(digit, RoundingMode.HALF_UP).floatValue());
            }
            else if (field.getType() == Double.class || field.getType() == double.class) {
                if (value.isEmpty()) {
                    field.set(t, 0.00D);
                } else {

                    field.set(t, new BigDecimal(value).setScale(digit, RoundingMode.HALF_UP).doubleValue());
                }

            }
            else if (field.getType() == Long.class || field.getType() == long.class) {
                if (value.isEmpty()) {
                    field.set(t, 0);
                }
                field.set(t, (long) Math.rint(Double.parseDouble(value)));
            }
            else if (field.getType() == LocalDate.class) {

                try {
                    field.set(t, LocalDate.parse(value));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    try {
                        field.set(t, LocalDate.parse(value.substring(0, "yyyyMMdd".length()),
                                DateTimeFormatter.ofPattern("yyyyMMdd")));
                    } catch (Exception date_e) {
                        try {

                            field.set(t, LocalDate.parse(value.substring(0, "yyyy-MM-dd".length()),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } catch (Exception date_e1) {
                            try {
                                field.set(t, LocalDate.parse(value.substring(0, "yyyy/MM/dd".length()),
                                        DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                            }catch(Exception date_e2) {
                                field.set(t,LocalDate.of(1970, 1, 1));
                            }

                        }

                    }
                }

            }
            else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                String lowValue=value.toLowerCase();
                String reg="对|是|有|是的|正确|yes|positive|right|correct|true|t|y|sure";
                field.set(t, lowValue.matches(reg));

            }
        } catch (IllegalAccessException | ParseException | java.text.ParseException e) {
            System.out.println("set "+t.getClass().getName()+"'s filed"+field.getName()+" to "+value+" error");
            e.printStackTrace();
        }
    }


    public static void writeFieldValueToCell(Object o,Field field,Cell cell) {

        field.setAccessible(true);
        Object value ;
        Object type ;
        try {
             value = field.get(o);
            if (value == null)
                return;

             type = field.getType();

            if (type == String.class) {
                cell.setCellValue((String) value);
            } else if (type == Integer.class||type==int.class) {
                cell.setCellValue((Integer) value);
            }
            else if (type == Long.class||type==long.class) {
                cell.setCellValue((Long) value);
            }
            else if (type == Double.class||type==double.class) {

                cell.setCellValue((Double) value);
            }
            else if (type == Float.class||type==float.class) {
                cell.setCellValue((Float) value);
            }
            else if (type == Date.class) {

                cell.setCellValue((Date) value);
            }
            else if (type == LocalDate.class) {
                LocalDate date=(LocalDate)value;
                String dateStr=date.toString();
                cell.setCellValue(dateStr);
            }
            else if (type == Boolean.class||type==boolean.class) {

                cell.setCellValue((Boolean)value);;
            }
            else {
                cell.setCellValue(value.toString());
            }

        } catch (Exception e) {
            System.out.println("writ to xls cell error: ");
            System.out.println(("type:" + field.getType() + " ,value:" + getFiledValue(o,field)));
            e.printStackTrace();
        }

    }
    private static Object getFiledValue(Object o, Field field){
        try {
            field.setAccessible(true);
            return field.get(o);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void stripe(Workbook workbook, int sheetIndex, int beginRowIndex) {
        Sheet sheet=workbook.getSheetAt(sheetIndex);
        boolean paint = false;
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int rowIndex = beginRowIndex; rowIndex <= sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null)
                continue;
            if (rowIndex>beginRowIndex) {    //遇到不同类的，变换flag;
                String preStr= ExcelUtil.getCellValueByCell(sheet.getRow(rowIndex-1).getCell(2))+
                        ExcelUtil.getCellValueByCell(sheet.getRow(rowIndex-1).getCell(0));

                String currentStr= ExcelUtil.getCellValueByCell(row.getCell(2))+
                        ExcelUtil.getCellValueByCell(row.getCell(0));

                if(!preStr.equals(currentStr)) {
                    paint = !paint;
                }

            }
            if (paint) {
                System.out.println("paint");
                for (int columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
                    Cell cell = row.getCell(columnIndex);
                    if (cell == null)
                        continue;
                    cell.setCellStyle(style);

                }

            }
        }
    }

    public static Field[] getFullFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        Class<?> superClass=clazz.getSuperclass();
        if(superClass!=null&&superClass!=Object.class) {
            fieldList.addAll(Arrays.asList(superClass.getDeclaredFields()));
        }
        Field[] fields=new Field[fieldList.size()];
        for(int i=0;i<fields.length;i++) {
            fields[i]=fieldList.get(i);
        }

        return fields;
    }

    public static void setObjectFields(Object o, Map<String,Integer> fieldNameToColumnIndex,Row row){
        for (Field field : o.getClass().getDeclaredFields()) {
            if(!fieldNameToColumnIndex.containsKey(field.getName())){
                continue;
            }
            int columnIndex=fieldNameToColumnIndex.get(field.getName());
            String cellValue=ExcelUtil.getCellValueByCell(row.getCell(columnIndex));
            ExcelUtil.setFieldValue(o, field, cellValue, 4);
        }
    }
    
}

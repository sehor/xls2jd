package com.pzr.xls2jd.core.util;

import com.pzr.xls2jd.core.domain.batchInvocie.InvoiceItem;
import com.pzr.xls2jd.core.domain.batchInvocie.Invoice;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.excelTool.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Util {

    public static List<RawInfo> filterAndCopy(List<RawInfo> origins, Predicate<RawInfo> predicate) {

        return origins.stream().filter(predicate).collect(Collectors.toList());
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
        Record record = new Record();
        record.set摘要(origin.getBrief());
        record.set日期(origin.getOccur_date());
        record.set科目名称(origin.getRelative_account());
        record.set科目代码(origin.getRelative_account_number() == null ? "未找到" : origin.getRelative_account_number());
        return record;
    }

    public static Record createBankRecord(RawInfo origin) {

        Record record = createRecord(origin);

        if (origin.getBank_income() > 0.01) {
            record.set借方金额(origin.getAmout());
        } else if (origin.getBank_pay() > 0.01) {
            record.set贷方金额(origin.getAmout());
        }


        record.set科目代码(origin.getBankNum());
        record.set科目名称("银行存款" + "_" + origin.getBankNum());

        return record;
    }

    public static void mergeSameClassification(List<RawInfo> origins) {
        Map<String, RawInfo> map = new HashMap<>();
        for (RawInfo origin : origins) {
            RawInfo mergeOrigin = map.get(origin.getRelative_account()); //以relative account为key merge
            if (mergeOrigin == null) {
                map.put(origin.getRelative_account(), origin);
            } else {

                mergeOrigin.setInvoice_amount(mergeOrigin.getInvoice_amount() + origin.getInvoice_amount());
                mergeOrigin.setInvoice_tax(mergeOrigin.getInvoice_tax() + origin.getInvoice_tax());
                mergeOrigin.setOccur_date(origin.getOccur_date().isAfter(mergeOrigin.getOccur_date()) ? origin.getOccur_date() : mergeOrigin.getOccur_date()); //选最晚的date
            }
        }
        origins.clear();
        origins.addAll(map.values());
    }

    public static List<InvoiceItem> readYYInvoiceXls(String filePath) {
        InputStream input = null;
        Workbook workbook = null;
        List<InvoiceItem> ans = new ArrayList<>();
        String preBuyer = "";
        try {
            File file = new File(filePath);
            input = new FileInputStream(file);
            workbook = file.getName().toLowerCase().endsWith("xlsx") ? new XSSFWorkbook(input)
                    : new HSSFWorkbook(input);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                //buyer可能空的
                String buyer = ExcelUtil.getCellValueByCell(row.getCell(0));
                if (buyer.isBlank()) {
                    buyer = preBuyer;
                } else {
                    preBuyer = buyer;
                }

                InvoiceItem ir = new InvoiceItem();

                ExcelUtil.setFieldValue(ir, ir.getClass().getDeclaredField("itemName"), ExcelUtil.getCellValueByCell(row.getCell(2)));
                ExcelUtil.setFieldValue(ir, ir.getClass().getDeclaredField("modelAndType"), ExcelUtil.getCellValueByCell(row.getCell(3)));
                ExcelUtil.setFieldValue(ir, ir.getClass().getDeclaredField("quantity"), ExcelUtil.getCellValueByCell(row.getCell(5)));
                ExcelUtil.setFieldValue(ir, ir.getClass().getDeclaredField("price"), ExcelUtil.getCellValueByCell(row.getCell(6)), 4);
                ExcelUtil.setFieldValue(ir, ir.getClass().getDeclaredField("notes"), ExcelUtil.getCellValueByCell(row.getCell(8)));
                ans.add(ir);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

    /**
     * 下载文件工具
     *
     * @param response response对象
     * @param realPath 文件路径
     * @param fileName 文件名称
     */
    public static void downloadUtil(final HttpServletResponse response, File file, String fileName) throws IOException {
        if (file.exists()) {
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                assert bis != null;
                bis.close();
                fis.close();
            }
        }
    }
    public static void downloadUtil(final HttpServletResponse response, byte[] data,String fileName) throws IOException {

        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-type", "application/octet-stream" + "; charset=utf-8");
            response.setContentType("application/octet-stream"  + ";charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void downloadUtil(final HttpServletResponse response, String data,String fileName) throws IOException {

        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-type", "application/octet-stream" + "; charset=utf-8");
            response.setContentType("application/octet-stream"  + ";charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.getOutputStream().write(data.getBytes());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
    }


}

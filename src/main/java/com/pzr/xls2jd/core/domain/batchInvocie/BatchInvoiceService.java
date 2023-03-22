package com.pzr.xls2jd.core.domain.batchInvocie;

import com.pzr.xls2jd.excelTool.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Author pzr
 * &#064;date:2023-02-21-6:01
 * @Description:
 **/
@Service
public class BatchInvoiceService {

    private final Map<String, String> xmlToFieldMap = new HashMap<>() {{
        put("购方名称", "buyerName");
        put("购方税号", "buyerTaxNum");
        put("购方银行账号", "buyerBankInfo");
        put("购方地址电话", "buyerAddrAndPhone");
        put("备注", "notes");
        put("复核人", "reviewer");
        put("收款人", "payee");
        put("商品编码版本号", "codeVersion");
        put("含税标志", "taxFlag");
        put("商品名称", "itemName");
        put("规格型号", "modelAndType");
        put("计量单位", "unitType");
        put("商品编码", "itemTaxNum");
        put("企业商品编码", "Qyspbm");
        put("优惠政策标识", "Syyhzcbz");
        put("零税率标识", "Lslbz");
        put("优惠政策说明", "Yhzcsm");
        put("单价", "price");
        put("数量", "quantity");
        put("金额", "amount");
        put("税率", "taxRate");
        put("扣除额", "Kce");
    }};

    public List<Invoice> readFromXls(InputStream input,String fileName){

        Workbook workbook = null;
        List<Invoice> invoices = new ArrayList<>();

        try {

            workbook = fileName.toLowerCase().endsWith("xlsx") ? new XSSFWorkbook(input)
                    : new HSSFWorkbook(input);
            Sheet sheet = workbook.getSheetAt(0);
            Row titleRow = sheet.getRow(0);
            Map<String, Integer> fieldNameToColumnIndex = new HashMap<>();

            for (int i = 0; i <= titleRow.getLastCellNum(); i++) {
                String value = ExcelUtil.getCellValueByCell(titleRow.getCell(i));
                if (xmlToFieldMap.containsKey(value)) {
                    fieldNameToColumnIndex.put(xmlToFieldMap.get(value), i);
                }
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);
                Invoice invoice = null;
                Cell buyerNameCell = row.getCell(fieldNameToColumnIndex.get("buyerName"));

                if (buyerNameCell != null && !buyerNameCell.getStringCellValue().isBlank()) {
                    invoice = new Invoice();
                    Buyer buyer = new Buyer();
                    ExcelUtil.setObjectFields(buyer, fieldNameToColumnIndex, row);
                    invoice.setBuyer(buyer);
                    ExcelUtil.setObjectFields(invoice, fieldNameToColumnIndex, row);
                    invoices.add(invoice);
                }
                if (invoice != null) {
                    InvoiceItem invoiceItem = new InvoiceItem();
                    ExcelUtil.setObjectFields(invoiceItem, fieldNameToColumnIndex, row);
                    invoice.getItems().add(invoiceItem);
                }


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

        return invoices;

    }

    public List<Invoice> readFromXls(File file) {
        try {
            return readFromXls(new FileInputStream(file),file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Invoice> sliceInvoice(Invoice originInvoice, double MaxAmount) {
        Invoice invoice = originInvoice.copy(); //复制一份
        Deque<InvoiceItem> items = new LinkedList<>(); //后进先出处理

        for (int i = invoice.getItems().size() - 1; i >= 0; i--) {
            items.push(invoice.getItems().get(i));
        }

        List<Invoice> splitInvoices = new ArrayList<>();//存放拆分后的发票
        double remainAmount = MaxAmount;
        Invoice splitInvoice = null;

        while (items.size() > 0) {
            InvoiceItem item = items.pop();
            if (splitInvoice == null || remainAmount <= 0) {
                splitInvoice = originInvoice.copy();
                splitInvoice.getItems().clear();
                remainAmount = MaxAmount;
                splitInvoices.add(splitInvoice);
            }

            if (item.getAmount() <= remainAmount) {
                splitInvoice.getItems().add(item);
                remainAmount -= item.getAmount();
            } else if (item.getAmount() > remainAmount) {
                InvoiceItem splitItem = splitItem(item, remainAmount);
                splitInvoice.getItems().add(splitItem);
                remainAmount = 0;
                item.setQuantity(item.getQuantity() - splitItem.getQuantity());
                items.push(item);
            }

        }


        return splitInvoices;
    }

    public List<Invoice> sliceInvoices(List<Invoice> invoices, double MaxAmount) {
        List<Invoice> splitInvoices = new ArrayList<>();
        for (Invoice invoice : invoices) {
            splitInvoices.addAll(sliceInvoice(invoice, MaxAmount));
        }
        return splitInvoices;
    }

    public void writeInvoicesToXml(List<Invoice> invoices, String path) {

        try {
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f), "gbk");
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(xmlString(invoices));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public  String  xmlString(List<Invoice> invoices){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\r");
        String head = """
                       <Kp>
                          <Version>2.0</Version>
                             <Fpxx>
                                <Zsl>%d</Zsl>                  
                                   <Fpsj>
                """.formatted(invoices.size());
        sb.append(head);

        sb.append(invoicesXml(invoices));

        String tail = """
                             </Fpsj>
                         </Fpxx>
                     </Kp>
                """;
        sb.append(tail);
        return sb.toString();
    }


    private String itemXml(InvoiceItem item, int index) {
        return """
                                    <Sph>
                                        <Xh>%d</Xh> 		         
                                        <Spmc>%s</Spmc>  
                                        <Ggxh>%s</Ggxh>  
                                        <Jldw>%s</Jldw>   
                                        <Spbm>%s</Spbm>  
                                        <Qyspbm>%s</Qyspbm> 
                                        <Syyhzcbz>%s</Syyhzcbz> 
                                        <Lslbz>%s</Lslbz > 
                                        <Yhzcsm>%s</Yhzcsm >  
                                        <Dj>%f</Dj> 	       
                                        <Sl>%f</Sl> 	
                                        <Je>%f</Je> 	
                                        <Slv>%f</Slv> 	
                                        <Kce>%f</Kce >   
                                    </Sph>
                """
                .formatted(index,
                        item.getItemName(),
                        item.getModelAndType(),
                        item.getUnitType(),
                        item.getItemTaxNum(),
                        item.getQyspbm(),
                        item.getSyyhzcbz(),
                        item.getLslbz(),
                        item.getYhzcsm(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getAmount(),
                        item.getTaxRate(),
                        item.getKce());
    }

    private String invoicesXml(List<Invoice> invoices) {
        StringBuilder sb = new StringBuilder();
        for (Invoice invoice : invoices) {
            Buyer buyer = invoice.getBuyer();
            String fphead = """
                    <Fp>
                        <Djh>%d</Djh >         
                        <Gfmc>%s</Gfmc>          
                        <Gfsh>%s</Gfsh>  
                        <Gfyhzh>%s</Gfyhzh>  
                        <Gfdzdh>%s</Gfdzdh>  
                        <Bz>%s</Bz> 		          
                        <Fhr>%s</Fhr> 	            
                        <Skr>%s</Skr> 		       
                        <Spbmbbh>%s</Spbmbbh>	
                        <Hsbz>%s</Hsbz>
                        <Spxx>
                              """
                    .formatted(invoices.size(),
                            buyer.getBuyerName(),
                            buyer.getBuyerTaxNum(),
                            buyer.getBuyerBankInfo(),
                            buyer.getBuyerAddrAndPhone(),
                            invoice.getNotes(),
                            invoice.getReviewer(),
                            invoice.getPayee(),
                            invoice.getCodeVersion(),
                            invoice.getTaxFlag());

            sb.append(fphead);
            for (int i = 0; i < invoice.getItems().size(); i++) {
                InvoiceItem item = invoice.getItems().get(i);

                sb.append(itemXml(item, i + 1));

            }
            sb.append("""
                        </Spxx>
                    </Fp>
                    """);
        }
        return sb.toString();
    }

    private InvoiceItem splitItem(InvoiceItem item, double remain) {
        InvoiceItem newItem = item.copy();
        double quantity = remain / item.getPrice();
        if (quantity >= 1000) {
            int q = (int) (quantity / 100);
            q = q * 100;
            newItem.setQuantity(q);
        } else if (quantity >= 100) {
            int q = (int) (quantity / 10);
            q = q * 10;
            newItem.setQuantity(q);
        } else if (quantity >= 1) {
            int q = (int) (quantity);
            newItem.setQuantity(q);
        } else {
            BigDecimal b = new BigDecimal(quantity);
            newItem.setQuantity(b.setScale(2, RoundingMode.FLOOR).doubleValue());
        }

        return newItem;
    }

    public static void main(String[] args) {
/*        //create a buyer instance
        Buyer buyer = new Buyer();
        buyer.setBuyerName("张三");
        buyer.setBuyerTaxNum("123456789");
        buyer.setBuyerBankInfo("中国银行");
        buyer.setBuyerAddrAndPhone("北京市海淀区");
//create a invoice instance
        Invoice invoice = new Invoice();
        invoice.setBuyer(buyer);
        invoice.setNotes("备注");
        invoice.setReviewer("复核人");
        invoice.setPayee("收款人");
        invoice.setCodeVersion("商品编码版本号");
        invoice.setTaxFlag("含税标志");
//create a invoiceItem instance
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setItemName("商品名称");
        invoiceItem.setModelAndType("规格型号");
        invoiceItem.setUnitType("计量单位");
        invoiceItem.setQyspbm("企业商品编码");
        invoiceItem.setQuantity(100);
        invoiceItem.setPrice(50);
        //create a invoiceItem instance
        InvoiceItem invoiceItem2 = new InvoiceItem();
        invoiceItem2.setItemName("商品名称2");
        invoiceItem2.setModelAndType("规格型号2");
        invoiceItem2.setUnitType("计量单位2");
        invoiceItem2.setQyspbm("企业商品编码2");
        invoiceItem2.setQuantity(20.65);
        invoiceItem2.setPrice(10.5);

        invoice.getItems().add(invoiceItem);
        invoice.getItems().add(invoiceItem2);
//create a batchInvoiceService instance
        BatchInvoiceService batchInvoiceService = new BatchInvoiceService();
//slice invoice
        List<Invoice> invoices = batchInvoiceService.sliceInvoice(invoice, 4900);
        invoices.forEach(
                invoice1 -> {
                    System.out.println(invoice1.getTotal() + "\t" + invoice1.getItems().size());
                    invoice1.getItems().forEach(
                            System.out::println
                    );
                }
        );

        batchInvoiceService.writeInvoicesToXml(invoices, "d:\\test.xml");*/

        BatchInvoiceService service2 = new BatchInvoiceService();
        List<Invoice> invoices1 = service2.readFromXls(new File("C:\\Users\\pzr\\Desktop\\开票信息样本.xls"));

        invoices1.forEach(
                invoice1 -> {
                    System.out.println(invoice1.getTotal() + "\t" + invoice1.getItems().size());
                    invoice1.getItems().forEach(
                            System.out::println
                    );
                }
        );
        service2.writeInvoicesToXml(service2.sliceInvoices(invoices1,112500), "d:\\test.xml");
        double sum=0;
        for (Invoice invoice : service2.sliceInvoices(invoices1,112500)) {
            sum+=invoice.getTotal();
        }
        System.out.println(new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).doubleValue());

    }
}



package com.pzr.xls2jd.pdf;

import com.pzr.xls2jd.core.domain.RawInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Author pzr
 * @date:2023-03-15-6:16
 * @Description:
 **/
public class PdfTools {
    private static String textFromPdf(String pdfPath) throws IOException {

        PDDocument document = PDDocument.load(new File(pdfPath));

        // Create a PDFTextStripper object to extract text from the document
        PDFTextStripper stripper = new PDFTextStripper();

        // Extract text from the document and store it in a String variable
        String text = stripper.getText(document);

        document.close();
        return text;
    }

    public static List<String> getLinesFromAnOuBankPDF(String pdfPath) {
        try {
            String text = textFromPdf(pdfPath);
            String[] split = text.split("\r\n");
            return Arrays.stream(split).filter(s -> s.startsWith("202")).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    //iterate a folder and get all pdf files
    private static File[] pdfs(String foldPath) {
        File file = new File(foldPath);
        if (file.isDirectory()) {
            return file.listFiles((dir, name) -> name.endsWith(".pdf"));
        } else {
            return null;
        }
    }

    private static RawInfo line2RawInfo(String line) {

        String[] split = line.replaceAll("\\s+", " ").split(" ");
        List<String> props = Arrays.stream(split).toList();
        RawInfo rawInfo = new RawInfo();
        rawInfo.setOccur_date(LocalDate.parse(props.get(0), DateTimeFormatter.ofPattern("yyyyMMdd")));
        boolean beginNum = false;
        for (int i = 1; i < props.size(); i++) {
            String s = props.get(i);
            if (s.replaceAll(",","").matches("[-+]?\\d+\\.\\d+")) {
                if (!beginNum) {
                    s = s.replaceAll(",", "");
                    if (s.startsWith("-")) {
                        rawInfo.setBank_pay(Double.parseDouble(s));
                    } else {
                        rawInfo.setBank_income(Double.parseDouble(s));
                    }
                    beginNum = true;
                }
            } else {
                if (!beginNum) {
                    rawInfo.setBrief(rawInfo.getBrief() + " " + s);
                } else {
                    rawInfo.setRelative_account(s);
                }
            }
        }

        return rawInfo;
    }

    public static void main(String[] args) {
        //test getLinesFromAnOuBankPDF function
       /* List<String> lines = getLinesFromAnOuBankPDF("C:\\Users\\pzr\\Desktop\\test.pdf");
        System.out.println(lines);*/
        String rowTest = "20220509 税款 6220509105086637 00TX:000990:20220509:99022050932 258.65 +13,675.13 中华人民共和国国家金库深圳分库";
        String[] split = rowTest.split("\\s+");
        //convert to list
        List<String> list = Arrays.stream(split).toList();
        //print each element
        //list.forEach(System.out::println);
        //select float number
        List<String> floats = list.stream().filter(s1 -> s1.replaceAll(",", "").matches("[-+]?\\d++\\.\\d++")).toList();
        //print float number
        floats.forEach(System.out::println);

        //test pdfs function
        File[] pdfs = pdfs("D:\\work\\finance\\回单\\anou\\对账单");
        List<String> allLines = Arrays.stream(pdfs).map(file -> getLinesFromAnOuBankPDF(file.getAbsolutePath())).filter(Objects::nonNull).flatMap(List::stream).toList();
        allLines.forEach(
                l -> {
                    RawInfo rawInfo = line2RawInfo(l);
                    System.out.println(rawInfo.getOccur_date()+"  "+ rawInfo.getBrief() + "   " + rawInfo.getBank_income() + "   " + rawInfo.getBank_pay() + "  " + rawInfo.getRelative_account());
                }
        );

        System.out.println("320.89".matches("[0123456789,?]\\.\\d{2}"));
    }
}


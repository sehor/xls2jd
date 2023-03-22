package com.pzr.xls2jd.core.controller;

import com.pzr.xls2jd.core.domain.batchInvocie.BatchInvoiceService;
import com.pzr.xls2jd.core.domain.batchInvocie.Invoice;
import com.pzr.xls2jd.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * @Author pzr
 * @date:2023-02-26-10:11
 * @Description:
 **/
@RestController
@RequestMapping("/batchInvoice")
public class BatchInvoiceController {
    @Autowired
    private BatchInvoiceService batchInvoiceService;

    //create a download template
    @RequestMapping("/download")
    public void downloadXml(HttpServletResponse response, @RequestParam(("fileName")) String fileName) {
        //todo
        try {
            Util.downloadUtil(response, "<a> test</a>", "text.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/upload")
    public void upLoad(HttpServletResponse response, @RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName) throws IOException {
        try {
            InputStream io = new ByteArrayInputStream(file.getBytes());
            List<Invoice> invoices = batchInvoiceService.readFromXls(io, fileName);
            List<Invoice> sliceInvoices = batchInvoiceService.sliceInvoices(invoices, 10000);
            String result= batchInvoiceService.xmlString(sliceInvoices);
            Util.downloadUtil(response, result, "text.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

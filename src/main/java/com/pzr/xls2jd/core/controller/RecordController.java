package com.pzr.xls2jd.core.controller;

import com.pzr.xls2jd.common.GlobalVarials;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-09-17:14
 * @Description:
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/record")
public class RecordController {
    private final RecordService recordService;

    @GetMapping("/fromDB")
    public List<Record> fromDB(@RequestParam("companyName")String companyName,String type,
                               @RequestParam(value = "begin", defaultValue = "2019-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                               @RequestParam(value = "end", defaultValue = "2019-01-31") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){

        List<Record> records = recordService.creatRecordsWithRawInfoFromMongo(companyName, type, begin, end);
        String path=GlobalVarials.FINANCE_DIR+companyName+"\\";
        String fileName=type+"_"+begin.getMonth()+".xls";
        File file=new File(path+fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recordService.writeRecordsToFile(records,file);
        return records;
    }
}

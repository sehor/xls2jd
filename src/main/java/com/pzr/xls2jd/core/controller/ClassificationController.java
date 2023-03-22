package com.pzr.xls2jd.core.controller;

import com.pzr.xls2jd.common.GlobalVarials;
import com.pzr.xls2jd.core.domain.Classification;
import com.pzr.xls2jd.proccesor.ProcessorProvider;
import com.pzr.xls2jd.core.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.File;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-09-6:52
 * @Description:
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/classification")
public class ClassificationController {
    private final ClassificationService classificationService;
    private final ProcessorProvider processorProvider;

    @GetMapping("/readFromFile")
    public List<Classification> readFromFile(@PathParam("companyName") String companyName){
        String filePath= GlobalVarials.FINANCE_DIR +companyName+"/"+companyName+"_科目列表.xls";
        File file=new File(filePath);

        return classificationService.readFromExcel(file, companyName,true);
    }

    @GetMapping("/fromDB")
    public List<Classification> fromDB(@RequestParam("companyName") String companyName){

        return classificationService.getFromDB(companyName);
    }

}

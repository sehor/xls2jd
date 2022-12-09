package com.pzr.xls2jd.core.controller;

import com.pzr.xls2jd.common.GlobalVarials;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.service.RawInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-04-15:00
 * @Description:
 **/
@RestController
@RequestMapping("/rawInfo")
@RequiredArgsConstructor
public class RawInfoController {
    private final RawInfoService rawInfoService;

    @GetMapping("/fromFile")
    public List<RawInfo> fromFile(@RequestParam("companyName") String companyName,
                                  @RequestParam("sheetName")String sheetName,
                                  @RequestParam("startRow") int startRow){
        String dir= GlobalVarials.FINANCE_DIR +companyName+"\\";
        String fileName=companyName+"_origin.xlsx";
        List<RawInfo> rawInfos = rawInfoService.getFromFile(companyName, dir + fileName, sheetName, 0, startRow - 1);
        rawInfoService.saveToDB(rawInfos);
        return rawInfos;

    }
}

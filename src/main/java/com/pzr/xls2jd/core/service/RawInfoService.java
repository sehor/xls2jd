package com.pzr.xls2jd.core.service;

import com.pzr.xls2jd.core.dao.RawInfoMongoRepository;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.excelTool.Reader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-05-9:51
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class RawInfoService {

    private  final RawInfoMongoRepository  rawInfoMongoRepository;
    private  final MongoOperations operations;

   public List<RawInfo> getFromFile(String companyName, String filePath, String sheetName, int headRow, int starRow) {
        File file = new File(filePath);
        if(!file.exists()){
            System.out.println("no such file:" + filePath);
            return List.of();
        }
        String bankNum=sheetName.replaceAll("\\D+","");
        String type=sheetName.replaceAll("_\\d+","");
        List<RawInfo> list = Reader.readFromExcel(RawInfo.class, file, sheetName, headRow, starRow);
        list.forEach(r->{
            r.setCompanyName(companyName);
            r.setId(companyName+"_"+type+"_"+r.getSerial_number());
            r.setType(type);
            r.setBankNum(bankNum);
        });
        return list;
    }

  public   List<RawInfo> findByPeriodAndTye(String companyName, String type, LocalDate begin, LocalDate end){
        return rawInfoMongoRepository.findByPeriodAndTye(companyName, type, begin, end);
    }

  public void saveToDB(List<RawInfo> rawInfos){
       rawInfos.forEach(operations::save);
  }

}

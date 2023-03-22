package com.pzr.xls2jd.core.service;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.Record;
import com.pzr.xls2jd.core.domain.TypeEnum;
import com.pzr.xls2jd.excelTool.Writer;
import com.pzr.xls2jd.proccesor.Processor;
import com.pzr.xls2jd.proccesor.ProcessorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author pzr
 * @date:2022-12-08-10:42
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class RecordService {

    private final RawInfoService rawInfoService;
    private final ProcessorProvider processorProvider;


    public  List<Record>  creatRecordsWithRawInfoFromMongo(String companyName, String type, LocalDate begin,LocalDate end){
        Processor processor = processorProvider.getProcessor(type);
        if(processor==null){
            System.out.println("no such type processor: " + type);
            return List.of();
        }

        //结转成本费用，使用Issue_Invoice的RawInfo
        if (TypeEnum.SoldToCost.value.equals(type)) {
            type=TypeEnum.Issue_Invoice.value;
        }
        List<RawInfo> rawInfos = rawInfoService.findByPeriodAndTye(companyName, type, begin, end);
        List<AccountEntry> accountEntries = new ArrayList<>();
        
        /*分会计期间处理*/
        LocalDate periodBegin=begin;
        LocalDate lastDay=begin.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate periodEnd = lastDay.isAfter(end)?end:lastDay;
        while (!periodBegin.isAfter(end)){
            ArrayList<RawInfo> filterRawInfos = new ArrayList<>();
            /*找出同一期间的*/
            for (RawInfo rawInfo : rawInfos) {
                if (!rawInfo.getOccur_date().isAfter(periodEnd) && !rawInfo.getOccur_date().isBefore(periodBegin)) {
                    filterRawInfos.add(rawInfo);
                }
            }
            List<RawInfo> preProcessRawInfos = processor.preProcess(filterRawInfos, companyName); //预处理
            accountEntries.addAll(processor.processToRecord(preProcessRawInfos,companyName)); //生成分录
            periodBegin=periodBegin.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()); //下一个月第一天
            lastDay = periodBegin.with(TemporalAdjusters.lastDayOfMonth());
            periodEnd=lastDay.isAfter(end)?end:lastDay; //不要超过end
        }

        return produceRecordsFromAccountEntries(accountEntries);
    }

    public void writeRecordsToFile(List<Record> records, File file){

        Writer.writeToFile(records,file,Record.class,true);
    }

    private List<Record> produceRecordsFromAccountEntries(List<AccountEntry> entries) {
        List<Record> records = new ArrayList<>();
        int entryNumb = 200;

        for (AccountEntry entry : entries) {
            entry.tidy();
            for (Record record : entry.records()) {
                record.set凭证字("记");
                record.set凭证号(entryNumb);
            }
            entryNumb++;
            records.addAll(entry.records());
        }
        return records;
    }


}

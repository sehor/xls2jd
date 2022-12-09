package com.pzr.xls2jd.core.service;

import com.pzr.xls2jd.excelTool.Reader;
import com.pzr.xls2jd.core.domain.Classification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-08-7:48
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class ClassificationService {
    private final MongoOperations operations;



    public List<Classification> readFromExcel(File file,String companyName,boolean saveToDB){
        List<Classification> list = Reader.readFromExcel(Classification.class, file, "科目列表", 0, 1);
        setFullName(list);
        list.forEach(c->{c.setCompanyName(companyName);c.setId(companyName+"_"+c.get编码());});
        if(saveToDB){
            list.forEach(operations::save);
        }
        return list;
    }

    public List<Classification> getFromDB(String companyName) {
        return operations.find(Query.query(Criteria.where("companyName").is(companyName)), Classification.class);
    }

    public String getNumByMutilName(String mutilName,String companyName){
        Classification one = operations.findOne(Query.query(Criteria.where("companyName").is(companyName).and("mutilName").is(mutilName))
                , Classification.class);
        return one!=null?one.get编码():"未找到";
    }

    private void setFullName(List<Classification> classifications){
        Deque<String[]> stack = new LinkedList<>();
        stack.push(new String[]{"",""});
        for (Classification c : classifications) {
            while (true) {
                String[] strs = stack.peek();
                assert strs != null;
                if (c.get编码().startsWith(strs[0])) {
                    c.setMutilName(!strs[1].isEmpty()?strs[1] + "-" + c.get名称():c.get名称());
                    stack.push(new String[]{c.get编码(), c.getMutilName()});
                    break;
                } else {
                    stack.pop();
                }
            }
        }

    }


}

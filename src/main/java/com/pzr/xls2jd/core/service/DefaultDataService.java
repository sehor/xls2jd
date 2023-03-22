package com.pzr.xls2jd.core.service;

import com.pzr.xls2jd.core.domain.RawInfo;
import com.pzr.xls2jd.core.domain.CompanyProperties;
import com.pzr.xls2jd.core.domain.TypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;


@RequiredArgsConstructor
@Service
public class DefaultDataService implements ProcessorService {
    private final CompanyProperties companyProperties;
    private final ClassificationService classificationService;
    private final MongoOperations operations;

    @Override
    public String getCompanyCnName(String name) {
        Map<String, String> map = companyProperties.getCompanies().getOrDefault(name, Map.of("name", "未找到"));
        return map.get("name");
    }

    @Override
    public String getAccountNumByMutilName(String mutilAccountName, String companyName) {
        // TODO Auto-generated method stub
        return classificationService.getNumByMutilName(mutilAccountName, companyName);
    }

    @Override
    public boolean isContainKeyWord(String text, String keyWord) {
        String[] strs = companyProperties.getKeyword().get(keyWord).split("\\|");
        for (String str : strs) {
            if (text.contains(str)) {

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean needToBeMerged(RawInfo origin) {

        return !origin.getType().equals(TypeEnum.Bank_Pay_Tax.value) ||
                !origin.getType().equals("Not_Found");  //未确定的和缴税业务不用合并（比如所得税，增值税不能合并处理）
    }

    @Override
    public String mergeMark(RawInfo origin) {
        // TODO Auto-generated method stub
        return origin.getType();
    }

    @Override
    public double findPersonSecurity(String companyName, LocalDate beginDay, LocalDate lastDay) {
        Query query = Query.query(Criteria.where("companyName").is(companyName).and("occur_date").gte(beginDay).lte(lastDay)
                .and("type").regex("Accrued_SalaryAndSecurity"));

        RawInfo rawInfo = operations.findOne(query, RawInfo.class);
        return rawInfo != null ? rawInfo.getPayedPersonSecurity() : 0;
    }

    @Override
    public double findSalary(String companyName, LocalDate beginDay, LocalDate lastDay) {
        Query query = Query.query(Criteria.where("companyName").is(companyName).and("occur_date").gte(beginDay).lte(lastDay)
                .and("type").regex("Bank"));
        double sum = 0;
        for (RawInfo rawInfo : operations.find(query, RawInfo.class)) {
            String text = rawInfo.getBank_brief1() + rawInfo.getBank_brief2();
            if (isContainKeyWord(text, "bankSalary")) {
                sum += rawInfo.getBank_pay();
            }
            ;
        }
        return sum;
    }

    @Override
    public double findPersonTax(String companyName, LocalDate beginDay, LocalDate lastDay) {
        Query query = Query.query(Criteria.where("companyName").is(companyName).and("occur_date").gte(beginDay).lte(lastDay)
                .and("type").regex("Bank"));
        double sum = 0;
        for (RawInfo rawInfo : operations.find(query, RawInfo.class)) {
            String text = rawInfo.getBank_brief1() + rawInfo.getBank_brief1();
            if (isContainKeyWord(text, "bankPersonalTax")) {
                sum += rawInfo.getBank_pay();
            }
            ;
        }
        return sum;
    }

    @Override
    public double findPersonFund(String companyName, LocalDate beginDay, LocalDate lastDay) {
        Query query = Query.query(Criteria.where("companyName").is(companyName).and("occur_date").gte(beginDay).lte(lastDay)
                .and("type").regex("Bank"));
        double sum = 0;
        for (RawInfo rawInfo : operations.find(query, RawInfo.class)) {
            String text = rawInfo.getBank_brief1() + rawInfo.getBank_brief1();
            if (isContainKeyWord(text, "bankHPFund")) {
                sum += rawInfo.getBank_pay() / 2;
            }
            ;
        }
        return sum;
    }

    @Override
    public String findRepresentiveOfCompany(String companyName) {
        return companyProperties.getCompanies().
                getOrDefault(companyName, Map.of("defChargePerson", "未找到法人代表")).get("defChargePerson");
    }


    @Override
    public Map<String, String> getBankMap() {
        // TODO Auto-generated method stub
        return Map.of("100201", "000 000 000");
    }

    @Override
    public String mainCostAccountNum(String companyName) {
        // TODO Auto-generated method stub
        return "5401";
    }

    @Override
    public String mainInvotoryAccounNum(String companyName) {
        // TODO Auto-generated method stub
        return "140501";
    }

    @Override
    public double grossProfitRate(String companyName) {
        return 0.9314;
    }

}

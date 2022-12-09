package com.pzr.xls2jd.core.dao;

import com.pzr.xls2jd.core.domain.RawInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-08-10:43
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class RawInfoMongoRepository {
  private  final MongoOperations operations;

  public   List<RawInfo> findByPeriodAndTye(String companyName, String type, LocalDate begin,LocalDate end){

      return operations.find(Query.query(Criteria.where("companyName").is(companyName).and("type").is(type)
              .and("occur_date").gte(begin).lte(end))
              ,RawInfo.class);
    }
}

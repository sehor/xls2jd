package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.AccountEntry;
import com.pzr.xls2jd.core.domain.RawInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author pzr
 * @date:2022-12-08-14:45
 * @Description:
 **/
@Service
public interface Processor {
    public List<RawInfo> preProcess(List<RawInfo> origins, String companyName);
    public List<AccountEntry> processToRecord(List<RawInfo> rawInfos,String companyName);

}

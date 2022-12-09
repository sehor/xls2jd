package com.pzr.xls2jd.proccesor;

import com.pzr.xls2jd.core.domain.TypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author pzr
 * @date:2022-12-08-14:56
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class ProcessorProvider {
    private final BankProcessor bankProcessor;
    private final BillProcessor billProcessor;
    private final ExpenseProcessor expenseProcessor;
    private final IssueInvoiceProcessor issueInvoiceProcessor;
    private final ReceiveInvoiceProcessor receiveInvoiceProcessor;
    private final SalaryAndSecurityProcessor salaryAndSecurityProcessor;
    private final SoldToCostProcessor soldToCostProcessor;
    private final VATProcessor vatProcessor;


    private  Map<String, Processor> processorMap = new HashMap<>();

   public void initMap(){

       this.processorMap=Map.of(TypeEnum.BanK.value,bankProcessor,
               TypeEnum.Receive_Invoice.value, receiveInvoiceProcessor,
               TypeEnum.Issue_Invoice.value,issueInvoiceProcessor,
               TypeEnum.Bill.value, billProcessor,
               TypeEnum.Expense.value, expenseProcessor,
               TypeEnum.Accrued_SalaryAndSecurity.value,salaryAndSecurityProcessor,
               TypeEnum.VAT.value,vatProcessor,
               TypeEnum.SoldToCost.value, soldToCostProcessor);
   }


   public Processor getProcessor(String type){

      return   processorMap.get(type);
   }
   public Map<String,Processor> processorMap(){
       return processorMap;
   }

}

package com.Car_Rental_API.module.sale_order.helper;


import com.Car_Rental_API.module.sale_order.repository.SaleOrderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// * Scheduled job for auto-advancing sale order statuses
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleOrderSchedulerService {

    private final SaleOrderRepository saleOrderRepository;

    // * Daily scheduler: Auto-advance order status (To Do -> In Progress -> Complete)
    @Scheduled(cron = "0 10 1 * * *")
    @Transactional
    @CacheEvict(value = {"saleOrders", "saleOrder", "saleOrderReportList", "saleOrderReportSummary"}, allEntries = true)
    public void autoAdvanceOrdersToInProgress() {
        int inProg = saleOrderRepository.autoAdvanceOrdersToInProgress();
        if (inProg > 0) log.info("[SaleOrderScheduler] Auto-advanced {} orders to In Progress.", inProg);

        int comp = saleOrderRepository.autoAdvanceOrdersToComplete();
        if (comp > 0) log.info("[SaleOrderScheduler] Auto-advanced {} orders to Complete.", comp);
    }
}

package com.bicycle.backtest.workflow.job.data;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.dataSource.BarDataSource;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.symbol.Exchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class BarTransposeJob {

    private final BarDataSource barDataSource;

    public void transpose(Exchange exchange, Timeframe timeframe, Map<Long, List<Bar>> data){
        log.info("Writing transpose data for {} dates", data.size());
        barDataSource.persist(exchange, timeframe, data);
    }
}

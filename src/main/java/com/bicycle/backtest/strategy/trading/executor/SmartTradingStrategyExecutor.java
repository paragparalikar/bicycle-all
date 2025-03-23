package com.bicycle.backtest.strategy.trading.executor;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.trading.TradingStrategyDefinition;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
public class SmartTradingStrategyExecutor implements TradingStrategyExecutor {
    
    private final IndicatorCache indicatorCache;
    private final BarRepository barRepository;
    private TradingStrategyExecutor serialTradingStrategyExecutor;
    private TradingStrategyExecutor parallelTradingStrategyExecutor;
    
    private TradingStrategyExecutor getSerialTradingStrategyExecutor() {
        return null == serialTradingStrategyExecutor ? 
                serialTradingStrategyExecutor = new SerialTradingStrategyExecutor(barRepository, indicatorCache)
                : serialTradingStrategyExecutor;
    }
    
    private TradingStrategyExecutor getParallelTradingStrategyExecutor() {
        return null == parallelTradingStrategyExecutor ? 
                parallelTradingStrategyExecutor = new ParallelTradingStrategyExecutor(barRepository, indicatorCache)
                : parallelTradingStrategyExecutor;
    } 

    @Override
    public void execute(TradingStrategyDefinition definition, long startDate,
            long endDate, ReportCache reportCache) {
        final TradingStrategyExecutor tradingStrategyExecutor = 
                Runtime.getRuntime().availableProcessors() * 10 < definition.getTradingStrategies().size() ?
                getParallelTradingStrategyExecutor() : getSerialTradingStrategyExecutor();
        System.out.println("Using " + tradingStrategyExecutor.getClass().getSimpleName() + " for execution");
        tradingStrategyExecutor.execute(definition, startDate, endDate, reportCache);
    }
    
}

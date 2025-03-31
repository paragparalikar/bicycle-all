package com.bicycle.backtest.executor;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.Backtest;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SmartBacktestExecutor implements BacktestExecutor {
    
    private final IndicatorCache indicatorCache;
    private final BarRepository barRepository;
    private BacktestExecutor serialTradingStrategyExecutor;
    private BacktestExecutor parallelTradingStrategyExecutor;
    
    private BacktestExecutor getSerialTradingStrategyExecutor() {
        return null == serialTradingStrategyExecutor ? 
                serialTradingStrategyExecutor = new SerialBacktestExecutor(barRepository, indicatorCache)
                : serialTradingStrategyExecutor;
    }
    
    private BacktestExecutor getParallelTradingStrategyExecutor() {
        return null == parallelTradingStrategyExecutor ? 
                parallelTradingStrategyExecutor = new ParallelBacktestExecutor(barRepository, indicatorCache)
                : parallelTradingStrategyExecutor;
    } 

    @Override
    public void execute(Backtest backtest, long startDate,
                        long endDate, ReportCache reportCache) {
        final BacktestExecutor tradingStrategyExecutor =
                Runtime.getRuntime().availableProcessors() * 10 < backtest.getTradingStrategies().size() ?
                getParallelTradingStrategyExecutor() : getSerialTradingStrategyExecutor();
        System.out.println("Using " + tradingStrategyExecutor.getClass().getSimpleName() + " for execution");
        tradingStrategyExecutor.execute(backtest, startDate, endDate, reportCache);
    }
    
}

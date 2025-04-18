package com.bicycle.backtest.executor;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.Backtest;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Symbol;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

@Builder
@RequiredArgsConstructor
public class ParallelBacktestExecutor implements BacktestExecutor {

    private final BarRepository barRepository;
    private final IndicatorCache indicatorCache;
    
    @Override
    @SneakyThrows
    public void execute(Backtest backtest, long startDate,
                        long endDate, ReportCache reportCache) {
        reportCache.clear();
        indicatorCache.clear();
        final Bar bar = new Bar();
        final List<TradingStrategyRunner> runners = createRunners(bar, backtest.getTradingStrategies());
        final IntList symbolCache = new IntArrayList(backtest.getSymbols().stream().map(Symbol::token).toList());
        for(Timeframe timeframe : backtest.getTimeframes()) {
            try(Cursor<Bar> cursor = barRepository.get(backtest.getExchange(), timeframe, startDate, endDate)){
                long previousBarDate = 0;
                for(int index = 0; index < cursor.size(); index++) {
                    cursor.advance(bar);
                    if(null != bar.symbol() && symbolCache.contains(bar.symbol().token())) {
                        indicatorCache.onBar(bar);
                        runners.parallelStream().forEach(TradingStrategyRunner::run);
                        if(previousBarDate != bar.date()) reportCache.compute(previousBarDate = bar.date());
                    }
                }
            }
        }
    }
    
    private List<TradingStrategyRunner> createRunners(Bar bar, Collection<MockTradingStrategy> tradingStrategies){
        final AtomicInteger counter = new AtomicInteger(0);
        final int processorCount = Runtime.getRuntime().availableProcessors();
        return tradingStrategies.stream()
            .collect(Collectors.groupingBy(strategy -> counter.incrementAndGet() / processorCount))
            .values().stream()
            .map(strategies -> new TradingStrategyRunner(bar, strategies))
            .toList();
    }

}

@RequiredArgsConstructor
class TradingStrategyRunner implements Runnable {
    
    private final Bar bar;
    private final List<MockTradingStrategy> tradingStrategies;
    
    @Override
    public void run() {
        for (MockTradingStrategy tradingStrategy : tradingStrategies) {
            tradingStrategy.onBar(bar);
        }
    }
    
}

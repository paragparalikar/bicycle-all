package com.bicycle.backtest.strategy.trading.executor;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.trading.TradingStrategyDefinition;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Symbol;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Builder
@RequiredArgsConstructor
public class SerialTradingStrategyExecutor implements TradingStrategyExecutor {

    private final BarRepository barRepository;
    private final IndicatorCache indicatorCache;
    
    @Override
    @SneakyThrows
    public void execute(TradingStrategyDefinition definition, long startDate,
            long endDate, ReportCache reportCache) {
        reportCache.clear();
        indicatorCache.clear();
        final Bar bar = new Bar();
        final IntList symbolCache = new IntArrayList(definition.getSymbols().stream().map(Symbol::token).toList());
        for(Timeframe timeframe : definition.getTimeframes()) {
            try(Cursor<Bar> cursor = barRepository.get(definition.getExchange(), timeframe, startDate, endDate)){
                long previousBarDate = 0;
                for(int index = 0; index < cursor.size(); index++) {
                    cursor.advance(bar);
                    if(null != bar.symbol() && symbolCache.contains(bar.symbol().token())) {
                        indicatorCache.onBar(bar);
                        definition.getTradingStrategies().forEach(tradingStrategy -> tradingStrategy.onBar(bar));
                        if(previousBarDate != bar.date()) reportCache.compute(previousBarDate = bar.date());
                    }
                }
            }
        }
    }

}

package com.bicycle.backtest.executor;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.Backtest;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.Dates;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Builder
@RequiredArgsConstructor
public class SerialBacktestExecutor implements BacktestExecutor {

    private final BarRepository barRepository;
    private final IndicatorCache indicatorCache;
    
    @Override
    @SneakyThrows
    public void execute(Backtest backtest, long startDate,
                        long endDate, ReportCache reportCache) {
        reportCache.clear();
        indicatorCache.clear();
        final Bar bar = new Bar();
        final IntList symbolCache = new IntArrayList(backtest.getSymbols().stream().map(Symbol::token).toList());
        System.out.println();
        for(Timeframe timeframe : backtest.getTimeframes()) {
            try(Cursor<Bar> cursor = barRepository.get(backtest.getExchange(), timeframe, startDate, endDate)){
                long previousBarDate = 0;
                for(int index = 0; index < cursor.size(); index++) {
                    cursor.advance(bar);
                    if(null != bar.symbol() && symbolCache.contains(bar.symbol().token())) {
                        indicatorCache.onBar(bar);
                        backtest.getTradingStrategies().forEach(tradingStrategy -> tradingStrategy.onBar(bar));
                        if(previousBarDate != bar.date()) {
                            reportCache.compute(previousBarDate = bar.date());
                            //System.out.print("\033[1A\033[2K");
                            System.out.printf("\033[1A\033[2KProcessed - %s\n", Dates.format(previousBarDate));
                        }
                    }
                }
            }
        }
    }

}

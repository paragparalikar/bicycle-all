package com.bicycle.backtest.report.cache;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SymbolTradingStrategyReportCache implements ReportCache {
    
    private final float initialMargin;
    private final ReportBuilder builder;
    private final long startDate, endDate;
    private final Object2ObjectOpenHashMap<Symbol, TradingStrategyReportCache> cache = new Object2ObjectOpenHashMap<>();

    @Override
    public void clear() {
        cache.values().forEach(reportFactory -> reportFactory.clear());
    }
    
    public Map<Symbol, Collection<Report>> findAll(){
       final Map<Symbol, Collection<Report>> results = new HashMap<>();
       cache.forEach((symbol, reportCache) -> results.put(symbol, reportCache.findAll()));
        return results;
    }

    @Override
    public Report get(Symbol symbol, MockTradingStrategy tradingStrategy) {
        return cache.computeIfAbsent(symbol, key -> 
                new TradingStrategyReportCache(initialMargin, startDate, endDate, builder))
                .get(symbol, tradingStrategy);
    }
    
    @Override
    public void compute(long date) {
        cache.values().forEach(reportFactory -> reportFactory.compute(date));
    }

}

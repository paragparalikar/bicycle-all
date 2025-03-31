package com.bicycle.backtest.report.cache;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class SymbolReportCache implements ReportCache {

    private final float initialMargin;
    private final long startDate, endDate;
    private final ReportBuilder reportBuilder;
    private final ConcurrentHashMap<Symbol, Report> cache = new ConcurrentHashMap<>();

    @Override
    public void clear() {
        cache.values().forEach(Report::clear);
    }

    public Collection<Report> findAll(){
        return cache.values();
    }

    @Override
    public Report get(Symbol symbol, MockTradingStrategy tradingStrategy) {
        return cache.computeIfAbsent(symbol,
                key -> reportBuilder.build(initialMargin, tradingStrategy, startDate, endDate));
    }

    @Override
    public void compute(long date) {
        for(Report report : cache.values()) report.compute(date);
    }
}

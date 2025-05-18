package com.bicycle.backtest.report.cache;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SingletonReportCache implements ReportCache {

    @Getter private Report report;
    private final float initialMargin;
    private final long startDate, endDate;
    private final ReportBuilder reportBuilder;

    @Override
    public void compute(long date) {
        if(null != report) report.compute(date);
    }
    
    @Override
    public void clear() {
        if(null != report) report.clear();
    }

    @Override
    public Report get(Symbol symbol, MockTradingStrategy tradingStrategy) {
        return null == report ? report = reportBuilder.build(initialMargin, tradingStrategy, startDate, endDate) : report;
    }

}

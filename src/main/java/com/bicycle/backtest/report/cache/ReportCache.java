package com.bicycle.backtest.report.cache;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;

public interface ReportCache {

    public interface Customizer {
        ReportCache customize(float initialMargin, long startDate, long endDate, ReportBuilder reportBuilder);
    }
    
    void clear();
    
    void compute(long date);
    
    Report get(Symbol symbol, MockTradingStrategy tradingStrategy);

}

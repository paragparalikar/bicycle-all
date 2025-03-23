package com.bicycle.backtest.report;

import com.bicycle.backtest.strategy.trading.MockTradingStrategy;

public interface ReportBuilder {

    public interface Customizer {
        ReportBuilder customize(int symbolCount);
    }
    
    Report build(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate);

}

package com.bicycle.backtest.report;

import com.bicycle.backtest.strategy.trading.MockTradingStrategy;

public interface ReportBuilder {

    Report build(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate);

}

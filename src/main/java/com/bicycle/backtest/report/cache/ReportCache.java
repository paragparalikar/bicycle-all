package com.bicycle.backtest.report.cache;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;

public interface ReportCache {

    int SINGLETON = 0, SYMBOL = 1, TRADING_STRATEGY = 2;

    static ReportCache of(float initialMargin, long startDate, long endDate, ReportBuilder reportBuilder, int options){
        return switch (options) {
            case SINGLETON -> new SingletonReportCache(initialMargin, startDate, endDate, reportBuilder);
            case SYMBOL -> new SymbolReportCache(initialMargin, startDate, endDate, reportBuilder);
            case TRADING_STRATEGY -> new TradingStrategyReportCache(initialMargin, startDate, endDate, reportBuilder);
            case (SYMBOL | TRADING_STRATEGY) -> new SymbolTradingStrategyReportCache(initialMargin, reportBuilder, startDate, endDate);
            default -> throw new UnsupportedOperationException();
        };
    }

    void clear();
    
    void compute(long date);

    Report get(Symbol symbol, MockTradingStrategy tradingStrategy);

}

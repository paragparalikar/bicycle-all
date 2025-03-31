package com.bicycle.backtest.report;

import com.bicycle.backtest.report.accumulator.DrawdownAccumulatorReport;
import com.bicycle.backtest.report.accumulator.EquityAccumulatorReport;
import com.bicycle.backtest.report.accumulator.PositionAccumulatorReport;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;

public interface ReportBuilder {

    int BASE = 0, FULL = 1, EQUITY = 2, DRAWDOWN = 4, POSITIONS = 8;

    static ReportBuilder of(int symbolCount, int options){
        ReportBuilder builder = 0 == options % 2 ? BaseReport.builder(symbolCount) : FullReport.builder(symbolCount);
        if(1 >= options) return builder;
        for(int index = 1; index <= 3; index++){
            if(0 == (options & (1 << index))) continue;
            builder = switch (index) {
                case 1 -> EquityAccumulatorReport.builder(builder);
                case 2 -> DrawdownAccumulatorReport.builder(builder);
                case 3 -> PositionAccumulatorReport.builder(builder);
                default -> throw new UnsupportedOperationException();
            };
        }
        return builder;
    }
    
    Report build(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate);

}

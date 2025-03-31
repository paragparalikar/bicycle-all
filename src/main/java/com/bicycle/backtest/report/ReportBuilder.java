package com.bicycle.backtest.report;

import com.bicycle.backtest.report.accumulator.DrawdownAccumulatorReport;
import com.bicycle.backtest.report.accumulator.EquityAccumulatorReport;
import com.bicycle.backtest.report.accumulator.PositionAccumulatorReport;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;

public interface ReportBuilder {

    @Deprecated
    public interface Customizer {
        ReportBuilder customize(int symbolCount);
    }


    int BASE = 0, FULL = 1, FEATURE = 2, EQUITY = 4, DRAWDOWN = 8, POSITIONS = 16;

    static ReportBuilder of(int symbolCount, FeatureReport.FeatureCaptor featureCaptor, int options){
        ReportBuilder builder = 0 == options % 2 ? BaseReport.builder(symbolCount) : FullReport.builder(symbolCount);
        if(1 >= options) return builder;
        for(int index = 1; index <= 4; index++){
            if(0 == (options & (1 << index))) continue;
            builder = switch (index) {
                case 1 -> FeatureReport.builder(builder, featureCaptor);
                case 2 -> EquityAccumulatorReport.builder(builder);
                case 3 -> DrawdownAccumulatorReport.builder(builder);
                case 4 -> PositionAccumulatorReport.builder(builder);
                default -> throw new UnsupportedOperationException();
            };
        }
        return builder;
    }
    
    Report build(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate);

}

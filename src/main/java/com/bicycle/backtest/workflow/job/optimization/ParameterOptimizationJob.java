package com.bicycle.backtest.workflow.job.optimization;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.accumulator.PositionAccumulatorReport;
import com.bicycle.backtest.report.cache.TradingStrategyReportCache;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class ParameterOptimizationJob implements OptimizationJob {
    
    private final OptimizationContext context;
    
    @Override
    public void optimize(long startDate, long endDate) {
        final TradingStrategyReportCache reportCache = new TradingStrategyReportCache(context.getInitialMargin(),
                startDate, endDate, PositionAccumulatorReport.builder(BaseReport.builder(context.getDefinition().getSymbols().size())));
        context.getTradingStrategyExecutor().execute(context.getDefinition(), startDate, endDate, reportCache);
        final List<Report> reports = new ArrayList<>(reportCache.findAll());
        
        final List<MockPosition> positions = reports.stream()
                .map(PositionAccumulatorReport.class::cast)
                .map(PositionAccumulatorReport::getPositions)
                .flatMap(Collection::stream)
                .toList();
        context.setPositions(positions);
        
        final List<Double> performanceScores = reports.stream() 
                .map(context.getPerformanceEvaluator()::evaluate)
                .toList();
        final List<MockTradingStrategy> tradingStrategies = reports.stream()
                .sorted(Comparator.comparing(report -> score(report, reports, performanceScores)))
                .map(Report::getTradingStrategy)
                .limit(10).toList();
        
        context.getDefinition().getTradingStrategies().clear();
        context.getDefinition().getTradingStrategies().addAll(tradingStrategies);
    }
    
    private double score(Report report, List<Report> reports, List<Double> performanceScores) {
        double sum = 0;

        return sum;
    }

}

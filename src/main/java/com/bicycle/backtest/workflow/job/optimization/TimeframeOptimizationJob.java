package com.bicycle.backtest.workflow.job.optimization;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.cache.TradingStrategyReportCache;
import com.bicycle.core.bar.Timeframe;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@RequiredArgsConstructor
public class TimeframeOptimizationJob implements OptimizationJob {
    
    private final OptimizationContext context;
    
    @Override
    public void optimize(long startDate, long endDate) {
        final Map<Timeframe, Double> timeframeScores = new HashMap<>();
        final TradingStrategyReportCache reportCache = new TradingStrategyReportCache(context.getInitialMargin(), 
                startDate, endDate, BaseReport.builder(context.getDefinition().getSymbols().size()));
        for(Timeframe timeframe : Arrays.asList(Timeframe.D)) {
            context.getTradingStrategyExecutor().execute(context.getDefinition(), startDate, endDate, reportCache);
            final double score = context.getRobustnessEvaluator().evaluate(reportCache.findAll(), 
                    context.getPerformanceEvaluator());
            timeframeScores.put(timeframe, score);
        }
        final List<Timeframe> timeframes = timeframeScores.entrySet().stream()
                .sorted(Entry.comparingByValue())
                .map(Entry::getKey)
                .limit(10)
                .toList();
        
        context.getDefinition().getTimeframes().clear();
        context.getDefinition().getTimeframes().addAll(timeframes);
    }

}

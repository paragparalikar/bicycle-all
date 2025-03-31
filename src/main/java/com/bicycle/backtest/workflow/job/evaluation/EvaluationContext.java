package com.bicycle.backtest.workflow.job.evaluation;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.executor.BacktestExecutor;
import com.bicycle.backtest.workflow.job.evaluation.monteCarlo.MonteCarloReport;
import com.bicycle.core.indicator.IndicatorCache;
import lombok.Data;

@Data
public class EvaluationContext {
    
    private Report report;
    private MonteCarloReport monteCarloReport;
    private Backtest tradingStrategyDefinition;
    
    private final float initialMargin;
    private final long startDate, endDate;
    private final IndicatorCache indicatorCache;
    private final BacktestExecutor tradingStrategyExecutor;

}

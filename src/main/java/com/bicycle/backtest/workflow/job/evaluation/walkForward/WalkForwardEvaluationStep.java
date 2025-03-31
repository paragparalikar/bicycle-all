package com.bicycle.backtest.workflow.job.evaluation.walkForward;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.workflow.job.evaluation.EvaluationContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WalkForwardEvaluationStep implements WalkForwardStep {

    private final EvaluationContext evaluationContext;

    @Override
    public void execute(long startDate, long endDate) {
        final Backtest tradingStrategyDefinition = evaluationContext.getTradingStrategyDefinition();
        //final ReportCache reportCache = new SingletonReportCache(evaluationContext.getReport());
        evaluationContext.getTradingStrategyExecutor().execute(
                tradingStrategyDefinition,
                evaluationContext.getStartDate(), 
                evaluationContext.getEndDate(), 
                null);
    }

}
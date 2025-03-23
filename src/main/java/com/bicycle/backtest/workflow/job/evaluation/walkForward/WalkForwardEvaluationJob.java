package com.bicycle.backtest.workflow.job.evaluation.walkForward;

import com.bicycle.backtest.workflow.job.evaluation.EvaluationContext;
import com.bicycle.backtest.workflow.job.evaluation.EvaluationJob;
import com.bicycle.backtest.workflow.job.optimization.OptimizationContext;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class WalkForwardEvaluationJob implements EvaluationJob {

    private final Duration trainDuration, testDuration;
    private final OptimizationContext optimizationContext;

    @Override
    public void evaluate(EvaluationContext evaluationContext) {
        
        final WalkForwardOptimizationStep optimizationStep = new WalkForwardOptimizationStep(evaluationContext, optimizationContext);
        final WalkForwardEvaluationStep evaluationStep = new WalkForwardEvaluationStep(evaluationContext);
        long trainStartDate = evaluationContext.getStartDate();
        long trainEndDate = trainStartDate + trainDuration.toMillis();
        long testEndDate = trainEndDate + testDuration.toMillis();
        while (testEndDate <=  evaluationContext.getEndDate()) {
            optimizationStep.execute(trainStartDate, trainEndDate);
            evaluationStep.execute(trainEndDate, testEndDate);
            trainStartDate = testEndDate;
            trainEndDate = trainStartDate + trainDuration.toMillis();
            testEndDate = trainEndDate + testDuration.toMillis();
        }
    }

}

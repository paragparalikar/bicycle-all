package com.bicycle.backtest.workflow.job.evaluation.walkForward;

public interface WalkForwardStep {

    void execute(long startDate, long endDate);
    
}

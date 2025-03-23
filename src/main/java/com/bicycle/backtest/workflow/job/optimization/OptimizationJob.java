package com.bicycle.backtest.workflow.job.optimization;

import java.time.ZonedDateTime;

public interface OptimizationJob {

    void optimize(long startDate, long endDate);
    
}

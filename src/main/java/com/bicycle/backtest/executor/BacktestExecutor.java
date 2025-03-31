package com.bicycle.backtest.executor;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.Backtest;

public interface BacktestExecutor {

    void execute(
            Backtest backtest,
            long startDate,
            long endDate,
            ReportCache reportCache);
    
}

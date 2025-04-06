package com.bicycle.backtest.feature;

import com.bicycle.backtest.report.ObservableReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.InterceptableReportCache;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeatureReportCacheInterceptor implements InterceptableReportCache.Interceptor {

    private final FeatureReportObserver featureReportObserver;

    @Override
    public Report onGet(Symbol symbol, MockTradingStrategy tradingStrategy, ReportCache reportCache) {
        return new ObservableReport(featureReportObserver, reportCache.get(symbol, tradingStrategy));
    }
}

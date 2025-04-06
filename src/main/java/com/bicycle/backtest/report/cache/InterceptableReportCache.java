package com.bicycle.backtest.report.cache;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InterceptableReportCache implements ReportCache {

    public interface Interceptor {
        default void onClear(ReportCache reportCache) {}
        default void onCompute(long date, ReportCache reportCache) {}
        default Report onGet(Symbol symbol, MockTradingStrategy tradingStrategy, ReportCache reportCache) {
            return reportCache.get(symbol, tradingStrategy);
        }
    }

    private final ReportCache delegate;
    private final Interceptor interceptor;

    @Override
    public void clear() {
        interceptor.onClear(delegate);
    }

    @Override
    public void compute(long date) {
        interceptor.onCompute(date, delegate);
    }

    @Override
    public Report get(Symbol symbol, MockTradingStrategy tradingStrategy) {
        return interceptor.onGet(symbol, tradingStrategy, delegate);
    }
}

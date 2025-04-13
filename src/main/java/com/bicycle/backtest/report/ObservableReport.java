package com.bicycle.backtest.report;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.ObservableReport.Observer;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class ObservableReport implements Report {
    
    public interface Observer {
        default void onCompute(long date, Report report) {}
        default void onOpen(MockPosition position, Report report) {}
        default void onClose(MockPosition position, Report report) {}
    }

    public static ReportBuilder builder(Observer observer, ReportBuilder delegateBuilder) {
        return new ObservableReportBuilder(observer, delegateBuilder);
    }
    
    @NonNull private final ObservableReport.Observer observer;
    @NonNull @Delegate private final Report delegate;

    @Override
    public void compute(long date) {
        delegate.compute(date);
        observer.onCompute(date, delegate);
    }
    
    @Override
    public void open(MockPosition trade) {
        delegate.open(trade);
        observer.onOpen(trade, delegate);
    }
    
    @Override
    public void close(MockPosition trade) {
        delegate.close(trade);
        observer.onClose(trade, delegate);
    }
    
    @Override
    @SuppressWarnings("unchecked") 
    public <T extends Report> T unwrap(Class<T> type) {
        if(type.isAssignableFrom(getClass())) return (T) this;
        return delegate.unwrap(type);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}

@RequiredArgsConstructor
class ObservableReportBuilder implements ReportBuilder {
    
    private final Observer observer;
    private final ReportBuilder delegateBuilder;

    @Override
    public Report build(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate) {
        return new ObservableReport(observer, delegateBuilder.build(initialMargin, tradingStrategy, startDate, endDate));
    }
    
}

package com.bicycle.backtest.report.callback;

import com.bicycle.backtest.report.CallbackReport;
import com.bicycle.backtest.report.Report;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class AccumulatorCallback<T> implements CallbackReport.Callback {

    private final Function<Report, T> valueExtractor;
    @Getter private final Map<Long, T> values = new ConcurrentHashMap<>();

    @Override
    public void onCompute(long date, Report report) {
        values.put(date, valueExtractor.apply(report));
    }
}

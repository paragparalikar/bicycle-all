package com.bicycle.backtest.report.callback;

import com.bicycle.backtest.report.ObservableReport;
import com.bicycle.backtest.report.Report;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class AccumulatorObserver<T> implements ObservableReport.Observer {

    private final Function<Report, T> valueExtractor;
    @Getter private final List<T> valuesAsList = new ArrayList<>();
    @Getter private final Map<Long, T> values = new ConcurrentHashMap<>();

    @Override
    public void onCompute(long date, Report report) {
        final T value = valueExtractor.apply(report);
        values.put(date, value);
        valuesAsList.add(value);
    }

}

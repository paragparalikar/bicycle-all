package com.bicycle.backtest.report.observer;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.ObservableReport;
import com.bicycle.backtest.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeObserver implements ObservableReport.Observer {
    private final List<ObservableReport.Observer> observers = new ArrayList<>();

    public CompositeObserver(ObservableReport.Observer... observers) {
        Collections.addAll(this.observers, observers);
    }

    @Override
    public void onClose(MockPosition position, Report report) {
        for (ObservableReport.Observer observer : observers) observer.onClose(position, report);
    }

    @Override
    public void onOpen(MockPosition position, Report report) {
        for (ObservableReport.Observer observer : observers) observer.onOpen(position, report);
    }

    @Override
    public void onCompute(long date, Report report) {
        for (ObservableReport.Observer observer : observers) observer.onCompute(date, report);
    }
}

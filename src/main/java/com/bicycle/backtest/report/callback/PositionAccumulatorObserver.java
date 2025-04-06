package com.bicycle.backtest.report.callback;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.ObservableReport;
import com.bicycle.backtest.report.Report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PositionAccumulatorObserver implements ObservableReport.Observer {
    private final Map<Integer, MockPosition> openPositions = new ConcurrentHashMap<>();
    private final List<MockPosition> closedPositions = new ArrayList<>();

    @Override
    public void onOpen(MockPosition position, Report report) {
        openPositions.put(position.getId(), position);
    }

    @Override
    public void onClose(MockPosition position, Report report) {
        closedPositions.add(openPositions.remove(position.getId()));
    }

    public List<MockPosition> getClosedPositions() {
        return closedPositions.stream().sorted(Comparator.comparing(MockPosition::getEntryDate)).toList();
    }

    public List<MockPosition> getOpenPositions() {
        return openPositions.values().stream().sorted(Comparator.comparing(MockPosition::getEntryDate)).toList();
    }
}

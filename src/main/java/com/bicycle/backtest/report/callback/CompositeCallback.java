package com.bicycle.backtest.report.callback;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.CallbackReport;
import com.bicycle.backtest.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeCallback implements CallbackReport.Callback {
    private final List<CallbackReport.Callback> callbacks = new ArrayList<>();

    public CompositeCallback(CallbackReport.Callback... callbacks) {
        Collections.addAll(this.callbacks, callbacks);
    }

    @Override
    public void onClose(MockPosition position, Report report) {
        for (CallbackReport.Callback callback : callbacks) callback.onClose(position, report);
    }

    @Override
    public void onOpen(MockPosition position, Report report) {
        for (CallbackReport.Callback callback : callbacks) callback.onOpen(position, report);
    }

    @Override
    public void onCompute(long date, Report report) {
        for (CallbackReport.Callback callback : callbacks) callback.onCompute(date, report);
    }
}

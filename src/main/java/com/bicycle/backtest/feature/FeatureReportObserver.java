package com.bicycle.backtest.feature;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.ObservableReport;
import com.bicycle.backtest.report.Report;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class FeatureReportObserver implements ObservableReport.Observer {

    private volatile boolean headersWritten;
    private final FeatureWriter featureWriter;
    private final FeatureCaptor onOpenFeatureCaptor, onCloseFeatureCaptor;
    private final Map<Integer, List<Float>> cache = new ConcurrentHashMap<>();

    public FeatureReportObserver(FeatureCaptor onOpenFeatureCaptor, FeatureCaptor onCloseFeatureCaptor, FeatureWriter featureWriter){
        this.featureWriter = featureWriter;
        this.onOpenFeatureCaptor = onOpenFeatureCaptor;
        this.onCloseFeatureCaptor = onCloseFeatureCaptor;
        writeHeaders();
    }

    private void writeHeaders(){
        final List<String> headers = new ArrayList<>();
        onOpenFeatureCaptor.captureHeaders(headers);
        onCloseFeatureCaptor.captureHeaders(headers);
        featureWriter.writeHeaders(headers);
    }

    @Override
    public void onOpen(MockPosition position, Report report) {
        final List<Float> features = cache.computeIfAbsent(position.getId(), key -> new ArrayList<>());
        onOpenFeatureCaptor.captureValues(position, features);
    }

    @Override
    public void onClose(MockPosition position, Report report) {
        final List<Float> features = cache.get(position.getId());
        onCloseFeatureCaptor.captureValues(position, features);
        featureWriter.writeValues(features);
        features.clear();
    }
}

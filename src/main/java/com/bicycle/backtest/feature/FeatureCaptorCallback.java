package com.bicycle.backtest.feature;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.CallbackReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.core.indicator.IndicatorCache;

import java.util.ArrayList;
import java.util.List;

public class FeatureCaptorCallback implements CallbackReport.Callback {

    private final List<Float> values = new ArrayList<>();
    private final FeatureGroup onOpenFeatureGroup, onCloseFeatureGroup;

    public FeatureCaptorCallback(IndicatorCache cache, float multiplier, int barCount, int...barCounts){
        this.onCloseFeatureGroup = new PositionFeatureGroup();
        this.onOpenFeatureGroup = new CompositeFeatureGroup(
                new BarFeatureGroup(cache, barCount),
                new BarSequenceFeatureGroup(cache, barCounts),
                new TrendFeatureGroup(cache, multiplier, barCounts),
                new VolatilityFeatureGroup(cache, multiplier, barCounts),
                new VolumeFeatureGroup(cache, multiplier, barCounts)
        );
    }

    @Override
    public void onOpen(MockPosition position, Report report) {
        onOpenFeatureGroup.captureValues(position, values);
    }

    @Override
    public void onClose(MockPosition position, Report report) {
        onCloseFeatureGroup.captureValues(position, values);
    }
}

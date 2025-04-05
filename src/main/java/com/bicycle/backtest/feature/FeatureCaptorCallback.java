package com.bicycle.backtest.feature;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.feature.captor.*;
import com.bicycle.backtest.report.CallbackReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.core.indicator.IndicatorCache;

import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true)
public class FeatureCaptorCallback implements CallbackReport.Callback {

    private final List<Float> values = new ArrayList<>();
    private final FeatureCaptor onOpenFeatureCaptor, onCloseFeatureCaptor;

    public FeatureCaptorCallback(IndicatorCache cache, float multiplier, int barCount, int...barCounts){
        this.onCloseFeatureCaptor = new PositionFeatureCaptor();
        this.onOpenFeatureCaptor = new CompositeFeatureCaptor(
                new BarFeatureCaptor(cache, barCount),
                new BarSequenceFeatureCaptor(cache, barCounts),
                new TrendFeatureCaptor(cache, multiplier, barCounts),
                new VolatilityFeatureCaptor(cache, multiplier, barCounts),
                new VolumeFeatureCaptor(cache, multiplier, barCounts)
        );
    }

    @Override
    public void onOpen(MockPosition position, Report report) {
        onOpenFeatureCaptor.captureValues(position, values);
    }

    @Override
    public void onClose(MockPosition position, Report report) {
        onCloseFeatureCaptor.captureValues(position, values);
    }
}

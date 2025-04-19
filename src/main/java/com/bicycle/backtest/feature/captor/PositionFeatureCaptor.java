package com.bicycle.backtest.feature.captor;

import com.bicycle.core.position.Position;

import java.util.Arrays;
import java.util.List;

public class PositionFeatureCaptor implements FeatureCaptor {

    @Override
    public void captureHeaders(List<String> headers) {
        headers.addAll(Arrays.asList(
                "PNL", "BAR_COUNT", "MFE", "MFE_BAR_COUNT", "MAE", "MAE_BAR_COUNT", "ETD","EDT_BAR_COUNT"));
    }

    @Override
    public void captureValues(Position position, List<Object> values) {
        values.add(position.getClosePercentProfitLoss());
        values.add((float) position.getBarCount());
        values.add(position.getMfe());
        values.add((float) position.getMfeBarCount());
        values.add(position.getMae());
        values.add((float) position.getMaeBarCount());
        values.add(position.getEtd());
        values.add((float)position.getEtdBarCount());
    }
}

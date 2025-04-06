package com.bicycle.backtest.feature.captor;

import com.bicycle.core.position.Position;

import java.util.Arrays;
import java.util.List;

public class PositionFeatureCaptor implements FeatureCaptor {

    @Override
    public void captureHeaders(List<String> headers) {
        headers.addAll(Arrays.asList("PNL", "MFE", "MAE"));
    }

    @Override
    public void captureValues(Position position, List<Float> values) {
        values.add(position.getClosePercentProfitLoss());
        values.add(position.getMfe());
        values.add(position.getMae());
    }
}

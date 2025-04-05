package com.bicycle.backtest.feature.captor;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.List;

public interface FeatureCaptor {

    interface Builder {
        FeatureCaptor build(IndicatorCache indicatorCache);
    }

    void captureHeaders(List<String> headers);

    void captureValues(Position position, List<Float> values);

}

package com.bicycle.backtest.feature;

import com.bicycle.backtest.MockPosition;

import java.util.List;

public interface FeatureGroup {

    void captureHeaders(List<String> headers);

    void captureValues(MockPosition position, List<Float> values);

}

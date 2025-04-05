package com.bicycle.backtest.feature.captor;

import com.bicycle.core.position.Position;

import java.util.List;

public interface FeatureCaptor {

    void captureHeaders(List<String> headers);

    void captureValues(Position position, List<Float> values);

}

package com.bicycle.backtest.feature.writer;

import java.util.List;

public interface FeatureWriter extends AutoCloseable {

    void writeHeaders(List<String> headers);

    void writeValues(List<Float> values);

}

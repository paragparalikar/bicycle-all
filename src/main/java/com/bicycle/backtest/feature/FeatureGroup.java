package com.bicycle.backtest.feature;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Symbol;

import java.util.List;

public interface FeatureGroup {

    void captureHeaders(List<String> headers);

    void captureValues(Symbol symbol, Timeframe timeframe, List<Float> values);

}

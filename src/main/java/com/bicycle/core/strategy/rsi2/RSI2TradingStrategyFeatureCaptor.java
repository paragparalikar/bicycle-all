package com.bicycle.core.strategy.rsi2;

import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.List;

public class RSI2TradingStrategyFeatureCaptor implements FeatureCaptor {

    public RSI2TradingStrategyFeatureCaptor(IndicatorCache cache){

    }

    @Override
    public void captureHeaders(List<String> headers) {
        headers.add("");
    }

    @Override
    public void captureValues(Position position, List<Float> values) {

    }
}

package com.bicycle.backtest.feature.captor;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;

import java.util.List;

public class CompositeFeatureCaptor implements FeatureCaptor {

    final FeatureCaptor[] featureCaptors;

    public CompositeFeatureCaptor(FeatureCaptor...children){
        this.featureCaptors = children;
    }

    @Override
    public void captureHeaders(List<String> headers) {
        for(FeatureCaptor featureCaptor : featureCaptors) featureCaptor.captureHeaders(headers);
    }

    @Override
    public void captureValues(Symbol symbol, Timeframe timeframe, Position position, List<Float> values) {
        for(FeatureCaptor featureCaptor : featureCaptors) featureCaptor.captureValues(symbol, timeframe, position, values);
    }
}

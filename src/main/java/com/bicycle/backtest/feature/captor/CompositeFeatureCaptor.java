package com.bicycle.backtest.feature.captor;

import com.bicycle.core.position.Position;

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
    public void captureValues(Position position, List<Object> values) {
        for(FeatureCaptor featureCaptor : featureCaptors) featureCaptor.captureValues(position, values);
    }
}

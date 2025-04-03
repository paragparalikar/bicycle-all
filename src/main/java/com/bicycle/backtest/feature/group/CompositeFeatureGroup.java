package com.bicycle.backtest.feature.group;

import com.bicycle.backtest.MockPosition;

import java.util.List;

public class CompositeFeatureGroup implements FeatureGroup {

    final FeatureGroup[] featureGroups;

    public CompositeFeatureGroup(FeatureGroup...children){
        this.featureGroups = children;
    }

    @Override
    public void captureHeaders(List<String> headers) {
        for(FeatureGroup featureGroup : featureGroups) featureGroup.captureHeaders(headers);
    }

    @Override
    public void captureValues(MockPosition position, List<Float> values) {
        for(FeatureGroup featureGroup : featureGroups) featureGroup.captureValues(position, values);
    }
}

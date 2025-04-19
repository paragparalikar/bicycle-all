package com.bicycle.backtest.feature.writer;

import java.util.List;

public class CompositeFeatureWriter implements FeatureWriter {

    private final FeatureWriter[] delegates;

    public CompositeFeatureWriter(FeatureWriter...delegates){
        this.delegates = delegates;
    }

    @Override
    public void writeHeaders(List<String> headers) {
        for(FeatureWriter delegate : delegates) delegate.writeHeaders(headers);
    }

    @Override
    public void writeValues(List<Object> values) {
        for(FeatureWriter delegate : delegates) delegate.writeValues(values);
    }

    @Override
    public void close() throws Exception {
        for(FeatureWriter delegate : delegates) delegate.close();
    }
}

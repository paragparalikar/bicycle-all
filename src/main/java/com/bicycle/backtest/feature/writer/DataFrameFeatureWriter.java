package com.bicycle.backtest.feature.writer;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

import java.util.ArrayList;
import java.util.List;

public class DataFrameFeatureWriter implements FeatureWriter {

    private DataFrame dataFrame;
    private StructType structType;
    private final List<Tuple> tuples = new ArrayList<>();

    @Override
    public void writeHeaders(List<String> headers) {
        final List<StructField> structFields = headers.stream().map(header -> new StructField(header, DataTypes.NullableFloatType)).toList();
        this.structType = new StructType(structFields);
    }

    @Override
    public void writeValues(List<Float> values) {
        tuples.add(Tuple.of(structType, values.toArray()));
    }

    @Override
    public void close() throws Exception {
        dataFrame = null;
        structType = null;
        tuples.clear();
    }

    public DataFrame getDataFrame(){
        return null == dataFrame ? dataFrame = DataFrame.of(structType, tuples) : dataFrame;
    }

}

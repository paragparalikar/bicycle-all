package com.bicycle.backtest.feature.captor;

import com.bicycle.core.position.Position;

import java.util.Arrays;
import java.util.List;

public class PositionFeatureCaptor implements FeatureCaptor {

    @Override
    public void captureHeaders(List<String> headers) {
        headers.addAll(Arrays.asList("PNL", /*"DAY_OF_WEEK",*/ "DURATION", "BAR_COUNT", "MFE", "MFE_BAR_COUNT", "MAE", "MAE_BAR_COUNT"));
    }

    @Override
    public void captureValues(Position position, List<Float> values) {
        values.add(position.getClosePercentProfitLoss());
        //final LocalDateTime entryTime = Dates.toLocalDateTime(position.getEntryDate());
        //values.add((float) entryTime.getDayOfWeek().ordinal());
        values.add(position.getDuration() / 60000f);
        values.add((float) position.getBarCount());
        values.add(position.getMfe());
        values.add((float) position.getMfeBarCount());
        values.add(position.getMae());
        values.add((float) position.getMaeBarCount());
    }
}

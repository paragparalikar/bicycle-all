package com.bicycle.backtest.feature;

import com.bicycle.backtest.MockPosition;
import com.bicycle.util.Dates;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class PositionFeatureGroup implements FeatureGroup {

    @Override
    public void captureHeaders(List<String> headers) {
        headers.addAll(Arrays.asList("PNL", "DAY_OF_WEEK", "DURATION", "BAR_COUNT", "MFE", "MFE_BAR_COUNT", "MAE", "MAE_BAR_COUNT"));
    }

    @Override
    public void captureValues(MockPosition position, List<Float> values) {
        values.add(position.getClosePercentProfitLoss());
        final LocalDateTime entryTime = Dates.toLocalDateTime(position.getEntryDate());
        values.add((float) entryTime.getDayOfWeek().ordinal());
        values.add(position.getDuration() / 60000f);
        values.add((float) position.getBarCount());
        values.add(position.getMfe());
        values.add((float) position.getMfeBarCount());
        values.add(position.getMae());
        values.add((float) position.getMaeBarCount());
    }
}

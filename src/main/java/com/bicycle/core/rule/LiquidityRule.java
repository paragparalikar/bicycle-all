package com.bicycle.core.rule;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import lombok.Builder;
import lombok.experimental.Delegate;

public class LiquidityRule implements Rule {
    
    @Delegate private final Rule delegate;
    
    public LiquidityRule(IndicatorCache cache) {
        this(cache, 5, 100000, 100000000, 15);
    }
    
    @Builder
    public LiquidityRule(IndicatorCache cache, float minClose, int minVolume, int minTurnover, int barCount) {

        // Close is always greater than minClose for last barCount bars
        final Rule minCloseRule = cache.close().greaterThan(minClose);
        final Rule minCloseForBarCountRule = cache.ruleSatisfiedStrength(minCloseRule, barCount).greaterThanOrEquals(barCount - 1);

        // Volume is always greater than minVolume for last barCount bars
        final Rule minVolumeRule = cache.volume().greaterThan(minVolume);
        final Rule minVolumeForBarCountRule = cache.ruleSatisfiedStrength(minVolumeRule, barCount).greaterThanOrEquals(barCount - 1);

        // Turnover is always greater than minTurnover for last barCount bars
        final Indicator turnoverIndicator = cache.volume().multipliedBy(cache.close());
        final Rule minTurnoverRule = turnoverIndicator.greaterThan(minTurnover);
        final Rule minTurnoverForBarCountRule = cache.ruleSatisfiedStrength(minTurnoverRule, barCount).greaterThanOrEquals(barCount - 1);

        this.delegate = minCloseForBarCountRule.and(minVolumeForBarCountRule).and(minTurnoverForBarCountRule);
    }

}

package com.bicycle.core.rule;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.BarSeries;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WaitForBarCountRule implements Rule {

    private final int barCount;
    private final IndicatorCache indicatorCache;
    
    @Override
    public boolean isSatisfied(Symbol symbol, Timeframe timeframe, Position trade) {
        if(null == trade) return false;
        final BarSeries barSeries = indicatorCache.barSeries(symbol, timeframe);
        for(int entryIndex = 0; entryIndex < barSeries.size(); entryIndex++) {
            final Bar entryBar = barSeries.get(entryIndex);
            if(entryBar.date() == trade.getEntryDate()) {
                if(entryIndex >= barCount) return true;
                else break;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return toText(barCount);
    }
    
    public static String toText(int barCount) {
        return "after " + barCount + " bars";
    }

}

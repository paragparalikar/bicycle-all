package com.bicycle.core.rule;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StopGainRule implements Rule {
    
    private final float atrMultiple;
    private final Indicator atrIndicator;

    @Override
    public boolean isSatisfied(Symbol symbol, Timeframe timeframe, Position trade) {
        if(null == trade) return false;
        final float atrValue = atrIndicator.getValue(symbol, timeframe);
        final float maxAllowedExcursion = atrValue * atrMultiple;
        final float atrFavorableExcursion = trade.getEntryType().multiplier() * (trade.getLtp() - trade.getEntryPrice()) / atrValue;
        if(atrFavorableExcursion >= maxAllowedExcursion) {
            trade.setExitPrice(trade.getLtp());
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "stopGain(" + atrMultiple + ")";
    }

}

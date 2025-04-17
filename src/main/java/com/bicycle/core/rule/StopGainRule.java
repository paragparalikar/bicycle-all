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
        if(trade.getMfe() >= atrMultiple){
            // Below line is there because we are calling tryEntry and tryExit only on close.
            // If the rule is satisfied before the close of the bar, we would act in real trading, instead of waiting for the close.
            final float atrValue = atrIndicator.getValue(symbol, timeframe);
            final float allowedMfe = atrValue * atrMultiple;
            trade.setExitPrice(trade.getEntryPrice() + trade.getEntryType().multiplier() * allowedMfe);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "stopGain(" + atrMultiple + ")";
    }

}

package com.bicycle.core.rule;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StopLossRule implements Rule {

    private final boolean trail;
    private final float atrMultiple;
    private final Indicator atrIndicator;

    @Override
    public boolean isSatisfied(Symbol symbol, Timeframe timeframe, Position trade) {
        if(null == trade) return false;
        final float allowedMae = -1 * atrIndicator.getValue(symbol, timeframe) * atrMultiple;  // MAE is always negative
        if(trail){
            final float currentExcursion = trade.getEntryType().multiplier() * (trade.getLtp() - trade.getEntryPrice());
            if(currentExcursion <=  trade.getMfe() + allowedMae){
                // Below line is there because we are calling tryEnter and tryExit only on close.
                // If the rule is satisfied before the close of the bar, we would act in real trading, instead of waiting for the close.
                trade.setExitPrice(trade.getEntryPrice() + trade.getEntryType().multiplier() * (trade.getMfe() + allowedMae));
                return true;
            }
        } else if(trade.getMae() <= allowedMae) {
            // Below line is there because we are calling tryEnter and tryExit only on close.
            // If the rule is satisfied before the close of the bar, we would act in real trading, instead of waiting for the close.
            trade.setExitPrice(trade.getEntryPrice() - trade.getEntryType().multiplier() * allowedMae);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "stopLoss(" + atrMultiple + ")";
    }

}

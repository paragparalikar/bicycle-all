package com.bicycle.core.indicator;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombinedIndicator implements Indicator {
    
    private final Indicator left, right;
    private final IndicatorOperator operator;

    @Override
    public float getValue(Symbol symbol, Timeframe timeframe) {
        return operator.apply(left.getValue(symbol, timeframe), right.getValue(symbol, timeframe));
    }

    @Override
    public void onBar(Bar bar) { }
    
    @Override
    public void clear() { }
    
    @Override
    public String toString() {
        return toText(left, operator, right);
    }
    
    public static String toText(Indicator left, IndicatorOperator operator, Indicator right) {
        return "(" + left + " " + operator + " " + right + ")";
    }

}

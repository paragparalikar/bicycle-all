package com.bicycle.backtest;

import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.BarListener;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class MockPosition extends Position implements BarListener {
    private static final AtomicInteger ID = new AtomicInteger(0);

    private final int id;
    private float mfe, mae;
    private int barCount, mfeBarCount, maeBarCount;
    
    public MockPosition(Symbol symbol, Timeframe timeframe, OrderType entryType) {
        super(symbol, timeframe, entryType);
        this.id = ID.getAndIncrement();
    }
    
    public float getEtd() {
        return Math.abs(entryPrice + mfe - exitPrice);
    }
    
    public int getEtdBarCount() {
        return barCount - mfeBarCount;
    }
    
    public void onBar(Bar bar) {
        barCount++;
        ltp = bar.close();
    }
    
    public void onPrice(float price) {
        ltp = price;
        final float excursion = price - entryPrice * entryType.multiplier();
        if(excursion > mfe) {
            mfe = excursion;
            mfeBarCount = barCount;
        }
        if(excursion < mae) {
            mae = excursion;
            maeBarCount = barCount;
        }
    }

    @Override
    public String toString() {
        return String.format("%6d %s %6d %s %2d %6s %2d %6s %2d", id, super.toString(), barCount,
                Constant.NUMBER_FORMAT.format(mfe), mfeBarCount,
                Constant.NUMBER_FORMAT.format(mae), maeBarCount,
                Constant.NUMBER_FORMAT.format(getEtd()), getEtdBarCount());
    }

    @Override
    public String toCSV() {
        return String.join(",", String.valueOf(id), super.toCSV(),String.valueOf(barCount),
                Constant.NUMBER_FORMAT.format(mfe), String.valueOf(mfeBarCount),
                Constant.NUMBER_FORMAT.format(mae), String.valueOf(maeBarCount),
                Constant.NUMBER_FORMAT.format(getEtd()), String.valueOf(getEtdBarCount()));
    }

    public static String getCsvHeaders(){
        return "ID," + Position.getCsvHeaders() + ",BAR_COUNT,MFE,MFE_BAR_COUNT,MAE,MAE_BAR_COUNT,ETD,ETC_BAR_COUNT";
    }
}

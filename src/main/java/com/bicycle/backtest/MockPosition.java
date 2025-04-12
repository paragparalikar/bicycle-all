package com.bicycle.backtest;

import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public class MockPosition extends Position {
    private static final AtomicInteger ID = new AtomicInteger(0);

    private final int id;

    public MockPosition(Symbol symbol, Timeframe timeframe, OrderType entryType) {
        super(symbol, timeframe, entryType);
        this.id = ID.getAndIncrement();
    }


    @Override
    public int hashCode(){
        return  id;
    }

    @Override
    public String toString() {
        return String.format("%6d %s %6d %s %2d %6s %2d %6s %2d", id, super.toString(), getBarCount(),
                Constant.NUMBER_FORMAT.format(getMfe()), getMfeBarCount(),
                Constant.NUMBER_FORMAT.format(getMae()), getMaeBarCount(),
                Constant.NUMBER_FORMAT.format(getEtd()), getEtdBarCount());
    }

    @Override
    public String toCSV() {
        return String.join(",", String.valueOf(id), super.toCSV(),String.valueOf(getBarCount()),
                Constant.NUMBER_FORMAT.format(getMfe()), String.valueOf(getMfeBarCount()),
                Constant.NUMBER_FORMAT.format(getMae()), String.valueOf(getMaeBarCount()),
                Constant.NUMBER_FORMAT.format(getEtd()), String.valueOf(getEtdBarCount()));
    }

    public static String getCsvHeaders(){
        return "ID," + Position.getCsvHeaders() + ",BAR_COUNT,MFE,MFE_BAR_COUNT,MAE,MAE_BAR_COUNT,ETD,ETC_BAR_COUNT";
    }
}

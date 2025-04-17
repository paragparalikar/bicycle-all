package com.bicycle.core.position;

import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.symbol.Symbol;
import lombok.Data;

import java.util.Date;

@Data
public class Position {

    private final Symbol symbol;
    private final Timeframe timeframe;
    private final OrderType entryType;
    private long entryDate;
    private int entryQuantity;
    private float entryPrice;
    private long exitDate;
    private float exitPrice;
    private int exitQuantity;
    private float mfe, mae, ltp; // TODO Update MAE and MFE at startup for all positions trailing stops to work
    private int barCount, mfeBarCount, maeBarCount;
    
    public void enter(long entryDate, int entryQuantity, float entryPrice) {
        this.entryDate = entryDate;
        this.entryQuantity = entryQuantity;
        this.entryPrice = entryPrice;
    }
    
    public void exit(long exitDate, int exitQuantity, float exitPrice) {
        this.exitDate = exitDate;
        this.exitQuantity = exitQuantity;
        this.exitPrice = exitPrice;
    }

    public void onPrice(float price, Indicator atrIndicator) {
        ltp = price;
        final float excursion = entryType.multiplier() * (price - entryPrice) / atrIndicator.getValue(symbol, timeframe);
        if(excursion > mfe) {
            mfe = excursion;
            mfeBarCount = barCount;
        }
        if(excursion < mae) {
            mae = excursion;
            maeBarCount = barCount;
        }
    }

    public float getEtd() {
        return Math.abs(entryPrice + mfe - exitPrice);
    }

    public int getEtdBarCount() {
        return barCount - mfeBarCount;
    }
    
    public float getClosePercentProfitLoss() {
        return entryType.multiplier() * (exitQuantity * exitPrice - entryQuantity * entryPrice) * 100 / (entryQuantity * entryPrice);
    }
    
    public float getOpenPercentProfitLoss() {
        return entryType.multiplier() * (entryQuantity * (0 == ltp ? entryPrice : ltp) - entryQuantity * entryPrice) * 100 / (entryQuantity * entryPrice);
    }
    
    public long getDuration() {
        return exitDate - entryDate;
    }
    
    public float getOpenEquity() {
        return entryType.multiplier() * (0 == ltp ? entryPrice : ltp) * entryQuantity;
    }
    
    public float getCloseEquity() {
        return entryType.multiplier() * exitPrice * exitQuantity;
    }
    
    public boolean isOpen() {
        return entryQuantity != exitQuantity;
    }
    
    @Override
    public String toString() {
        return String.format("%-4s %12s %-3s %s %6s %5d %s %6s %5d %5s", entryType.name(), symbol.code(), timeframe.name(),
                Constant.DATE_FORMAT.format(new Date(entryDate)), Constant.NUMBER_FORMAT.format(entryPrice), entryQuantity,
                Constant.DATE_FORMAT.format(new Date(exitDate)), Constant.NUMBER_FORMAT.format(exitPrice), exitQuantity,
                Constant.NUMBER_FORMAT.format(getClosePercentProfitLoss()));
    }

    public static String getCsvHeaders(){
        return "TYPE,SYMBOL,TIMEFRAME,ENTRY_DATE,ENTRY_PRICE,ENTRY_QUANTITY,EXIT_DATE,EXIT_PRICE,EXIT_QUANTITY,PNL_PCT";
    }

    public String toCSV(){
        return String.join(",", entryType.name(), symbol.code(), timeframe.name(),
                Constant.DATE_FORMAT.format(new Date(entryDate)), Constant.NUMBER_FORMAT.format(entryPrice), String.valueOf(entryQuantity),
                Constant.DATE_FORMAT.format(new Date(exitDate)), Constant.NUMBER_FORMAT.format(exitPrice), String.valueOf(exitQuantity),
                String.valueOf(getClosePercentProfitLoss()));
    }
}

package com.bicycle.core.position;

import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.symbol.Symbol;
import lombok.Data;

import java.util.Date;

@Data
public class Position {

    protected final Symbol symbol;
    protected final Timeframe timeframe;
    protected final OrderType entryType;
    protected long entryDate;
    protected int entryQuantity;
    protected float entryPrice, entryAtr, ltp;
    protected long exitDate;
    protected float exitPrice;
    protected int exitQuantity;
    
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

package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.bar.BarDataAmiBrokerConverterStage;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;

import java.util.Arrays;
import java.util.function.Predicate;

public class BarDataConversionWorkflow {

    public static void main(String[] args) throws Exception {
        final Exchange exchange = Exchange.NSE;
        final Predicate<Symbol> symbolPredicate = KiteSymbolDataProvider.equitiesAndIndices()
                .and(KiteSymbolDataProvider.indices());
        final BarDataAmiBrokerConverterStage barDataAmiBrokerConverterStage = new BarDataAmiBrokerConverterStage();
        for(Timeframe timeframe : Arrays.asList(Timeframe.D)){
            barDataAmiBrokerConverterStage.execute(exchange, timeframe, symbolPredicate);
        }
    }

}

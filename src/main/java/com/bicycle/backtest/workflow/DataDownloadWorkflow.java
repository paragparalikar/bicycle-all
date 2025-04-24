package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.BarDataDownloadStage;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;

import java.util.List;

public class DataDownloadWorkflow {

    public static void main(String[] args) {
        final Exchange exchange = Exchange.NSE;
        final Timeframe timeframe = Timeframe.D;
        new BarDataDownloadStage().execute(List.of(exchange), List.of(timeframe),
                KiteSymbolDataProvider.equitiesAndIndices());
    }

}

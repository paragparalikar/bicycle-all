package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.BarDataDownloadStage;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;

import java.util.List;
import java.util.Set;

public class DataDownloadWorkflow {

    public static void main(String[] args) {
        final Set<String> symbolCodes = Set.of("NIFTY 50", "NIFTY BANK", "NIFTY FIN SERVICE",
                "NIFTY MID SELECT", "NIFTY NEXT 50");
        new BarDataDownloadStage().execute(
                List.of(Exchange.NSE),
                List.of(Timeframe.M10, Timeframe.M5, Timeframe.M3, Timeframe.M1),
                symbol -> symbolCodes.contains(symbol.code()));
    }

}

package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.bar.BarDataTransposeStage;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;

import java.util.List;

public class BarDataTransposeWorkflow {

    public static void main(String[] args) {

        new BarDataTransposeStage().execute(List.of(Exchange.NSE), List.of(Timeframe.values()));

    }

}

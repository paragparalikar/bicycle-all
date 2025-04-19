package com.bicycle.backtest.workflow.stage;

import smile.data.DataFrame;
import smile.data.formula.Formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureImputationStage {

    public DataFrame execute(Formula formula, DataFrame dataFrame){
        System.out.println("\n--------------- Initiating feature imputation stage ---------------");
        final List<String> ineligibleColumnNames = new ArrayList<>(Arrays.asList("PNL", "BAR_COUNT", "MFE", "MFE_BAR_COUNT",
                "MAE", "MAE_BAR_COUNT", "ETD","EDT_BAR_COUNT", "SYMBOL_EFFICIENCY", "SYMBOL_VOLATILITY", "SYMBOL_SPREAD",
                "SYMBOL_TRUE_RANGE", "SYMBOL_VOLUME", "SYMBOL_TURNOVER"));
        ineligibleColumnNames.remove(formula.y(dataFrame).name());
        dataFrame = dataFrame.drop(ineligibleColumnNames.toArray(String[]::new));
        ineligibleColumnNames.forEach(name -> System.out.println("Dropped column " + name));
        System.out.println("Dropping all rows having null values");
        System.out.println("Row count before imputation : " + dataFrame.nrow());
        dataFrame = dataFrame.dropna();
        System.out.println("Row count after imputation : " + dataFrame.nrow());
        return dataFrame;
    }

}

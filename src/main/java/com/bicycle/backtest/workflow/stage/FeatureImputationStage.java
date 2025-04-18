package com.bicycle.backtest.workflow.stage;

import smile.data.DataFrame;

import java.util.Arrays;
import java.util.List;

public class FeatureImputationStage {

    public DataFrame execute(DataFrame dataFrame){
        System.out.println("--------------- Initiating feature imputation stage ---------------");
        final List<String> ineligibleColumnNames = Arrays.asList("PNL", "BAR_COUNT", "MFE_BAR_COUNT", "MAE", "MAE_BAR_COUNT", "ETD","EDT_BAR_COUNT");
        dataFrame = dataFrame.drop(ineligibleColumnNames.toArray(String[]::new));
        ineligibleColumnNames.forEach(name -> System.out.println("Dropped column " + name));
        System.out.println("Dropping all rows having null values");
        System.out.println("Row count before imputation : " + dataFrame.nrow());
        dataFrame = dataFrame.dropna();
        System.out.println("Row count after imputation : " + dataFrame.nrow());
        return dataFrame;
    }

}

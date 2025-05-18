package com.bicycle.backtest.workflow.stage.feature;

import smile.clustering.CentroidClustering;
import smile.clustering.GMeans;
import smile.data.DataFrame;
import smile.data.vector.ValueVector;
import smile.feature.transform.RobustStandardizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeatureClusteringStage {

    public List<List<String>> findClusterNames(final DataFrame dataFrame) {
        System.out.println("\n--------------- Initiating feature clustering stage ---------------");
        final DataFrame transformedDataFrame = RobustStandardizer.fit(dataFrame).apply(dataFrame);
        final double[][] data = transformedDataFrame.toMatrix().transpose().toArray();
        final CentroidClustering<double[], double[]> clusterer = GMeans.fit(data, 150, 100);
        final List<List<String>> results = cluster(transformedDataFrame, clusterer);
        print(results);
        return results;
    }

    public DataFrame toCentroidsDataFrame(final DataFrame dataFrame){
        System.out.println("\n--------------- Initiating feature clustering stage ---------------");
        final DataFrame transformedDataFrame = RobustStandardizer.fit(dataFrame).apply(dataFrame);
        final double[][] data = transformedDataFrame.toMatrix().transpose().toArray();
        final CentroidClustering<double[], double[]> clusterer = GMeans.fit(data, 500, 100);
        final Map<double[], String> vectorNames = transformedDataFrame.columns().stream().collect(
                Collectors.toMap(ValueVector::toDoubleArray, ValueVector::name));
        final String[] centroids = new String[clusterer.k()];
        for(int index = 0; index < clusterer.k(); index++){
            centroids[index] = vectorNames.get(clusterer.center(index));
            System.out.printf("Centroid for Cluster %d : %s\n", index, centroids[index]);
        }
        return dataFrame.select(centroids);
    }

    private List<List<String>> cluster(final DataFrame dataFrame, final CentroidClustering<double[], double[]> clusterer){
        final List<List<String>> results = new ArrayList<>(clusterer.k());
        for(int index = 0; index < clusterer.k(); index++) results.add(new ArrayList<>());
        for(String name : dataFrame.names()){
            final double[] values = dataFrame.column(name).toDoubleArray();
            final int clusterIndex = clusterer.predict(values);
            results.get(clusterIndex).add(name);
        }
        return results;
    }

    private void print(List<List<String>> results){
        System.out.printf("Results of feature clustering (%d clusters) are as below:\n", results.size());
        results.sort(Comparator.comparing(List::size));
        results.stream().map(cluster -> String.join(",", cluster)).forEach(System.out::println);
    }

}

package com.bicycle.backtest.workflow.stage.feature;

import smile.clustering.CentroidClustering;
import smile.clustering.GMeans;
import smile.data.DataFrame;
import smile.data.vector.ValueVector;
import smile.feature.transform.RobustStandardizer;

import java.util.*;
import java.util.stream.Collectors;

public class FeatureClusteringStage {

    public List<List<String>> findClusterNames(final DataFrame dataFrame) {
        System.out.println("\n--------------- Initiating feature clustering stage ---------------");
        final DataFrame transformedDataFrame = RobustStandardizer.fit(dataFrame).apply(dataFrame);
        final double[][] data = transformedDataFrame.toMatrix().transpose().toArray();
        final CentroidClustering<double[], double[]> clusterer = GMeans.fit(data, dataFrame.ncol(), 100);
        final List<List<String>> results = cluster(transformedDataFrame, clusterer);
        print(results);
        return results;
    }

    public DataFrame toCentroidsDataFrame(final DataFrame dataFrame, String targetVariableName){
        System.out.println("\n--------------- Initiating feature clustering stage ---------------");
        final DataFrame featuresDataFrame = dataFrame.drop(targetVariableName);
        final DataFrame transformedDataFrame = RobustStandardizer.fit(featuresDataFrame).apply(featuresDataFrame);
        final double[][] data = transformedDataFrame.toMatrix().transpose().toArray();
        System.out.println("Data has been transformed, now initiating clustering, this could take a while...");
        final CentroidClustering<double[], double[]> clusterer = GMeans.fit(data, transformedDataFrame.ncol(), 100);
        System.out.printf("Clustering completed, found %d clusters\n", clusterer.k());
        final List<String> centroids = Arrays.stream(clusterer.group())
                .distinct()
                .mapToObj(transformedDataFrame::column)
                .map(ValueVector::name)
                .peek(name -> System.out.printf("Centroid : %s\n", name))
                .collect(Collectors.toCollection(ArrayList::new));
        centroids.add(targetVariableName);
        return dataFrame.select(centroids.toArray(String[]::new));
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

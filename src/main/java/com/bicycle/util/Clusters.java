package com.bicycle.util;

import lombok.experimental.UtilityClass;
import smile.math.distance.EuclideanDistance;

@UtilityClass
public class Clusters {

    public double computeSilhouetteScore(double[][] data, int[] labels, int k) {
        int n = data.length;
        double[] silhouetteScores = new double[n];
        EuclideanDistance distanceMetric = new EuclideanDistance();

        for (int i = 0; i < n; i++) {
            int cluster = labels[i];
            double a = 0.0; // Average distance to points in the same cluster
            double[] b = new double[k]; // Average distance to points in other clusters
            int[] clusterCounts = new int[k]; // Count points in each cluster

            // Initialize b array with infinity
            for (int j = 0; j < k; j++) {
                b[j] = Double.POSITIVE_INFINITY;
            }

            // Compute distances to all other points
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                double distance = distanceMetric.d(data[i], data[j]);
                if (labels[j] == cluster) {
                    a += distance;
                    clusterCounts[cluster]++;
                } else {
                    b[labels[j]] = b[labels[j]] == Double.POSITIVE_INFINITY
                            ? distance
                            : b[labels[j]] + distance;
                    clusterCounts[labels[j]]++;
                }
            }

            // Compute average a
            a = clusterCounts[cluster] > 0 ? a / clusterCounts[cluster] : 0.0;

            // Compute average b for each other cluster and take the minimum
            for (int j = 0; j < k; j++) {
                if (j != cluster && clusterCounts[j] > 0) {
                    b[j] /= clusterCounts[j];
                }
            }
            double minB = Double.POSITIVE_INFINITY;
            for (int j = 0; j < k; j++) {
                if (j != cluster && b[j] != Double.POSITIVE_INFINITY) {
                    minB = Math.min(minB, b[j]);
                }
            }

            // Compute silhouette score for point i
            if (clusterCounts[cluster] > 0 && minB != Double.POSITIVE_INFINITY) {
                silhouetteScores[i] = (minB - a) / Math.max(a, minB);
            } else {
                silhouetteScores[i] = 0.0; // Singleton cluster or invalid case
            }
        }

        // Compute average silhouette score
        double avgSilhouette = 0.0;
        for (double score : silhouetteScores) {
            avgSilhouette += score;
        }
        return avgSilhouette / n;
    }

}

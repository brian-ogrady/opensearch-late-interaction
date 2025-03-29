/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.plugin.vector.util;

import java.util.List;

/**
 * Utility class for vector operations used in late interaction models.
 */
public class VectorUtils {

    /**
     * Computes the MaxSim score between query vectors and document vectors.
     * For each query vector, finds the maximum similarity with any document vector,
     * then sums these maximum similarities.
     *
     * @param queryVectors List of query token vectors
     * @param docVectors List of document token vectors
     * @param similarityFunction The similarity function to use (dot_product, cosine, etc.)
     * @return The MaxSim score
     */
    public static float computeMaxSim(
            List<List<Float>> queryVectors,
            List<List<Float>> docVectors,
            String similarityFunction) {
        
        float totalScore = 0.0f;
        
        // For each query vector, find the maximum similarity with any document vector
        for (List<Float> queryVector : queryVectors) {
            float maxSimilarity = Float.NEGATIVE_INFINITY;
            
            for (List<Float> docVector : docVectors) {
                float similarity;
                
                switch (similarityFunction.toLowerCase()) {
                    case "dot_product":
                        similarity = dotProduct(queryVector, docVector);
                        break;
                    case "cosine":
                        similarity = cosineSimilarity(queryVector, docVector);
                        break;
                    default:
                        throw new IllegalArgumentException(
                            "Unsupported similarity function: " + similarityFunction);
                }
                
                maxSimilarity = Math.max(maxSimilarity, similarity);
            }
            
            totalScore += maxSimilarity;
        }
        
        return totalScore;
    }

    /**
     * Computes the dot product of two vectors.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return The dot product
     */
    public static float dotProduct(List<Float> v1, List<Float> v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException(
                "Vector dimensions must match: " + v1.size() + " vs " + v2.size());
        }
        
        float sum = 0.0f;
        for (int i = 0; i < v1.size(); i++) {
            sum += v1.get(i) * v2.get(i);
        }
        
        return sum;
    }

    /**
     * Computes the cosine similarity between two vectors.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return The cosine similarity
     */
    public static float cosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException(
                "Vector dimensions must match: " + v1.size() + " vs " + v2.size());
        }
        
        float dotProduct = dotProduct(v1, v2);
        float norm1 = computeNorm(v1);
        float norm2 = computeNorm(v2);
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0f; // Avoid division by zero
        }
        
        return dotProduct / (norm1 * norm2);
    }

    /**
     * Computes the L2 norm (Euclidean length) of a vector.
     *
     * @param vector The vector
     * @return The L2 norm
     */
    private static float computeNorm(List<Float> vector) {
        float sumSquares = 0.0f;
        for (float value : vector) {
            sumSquares += value * value;
        }
        return (float) Math.sqrt(sumSquares);
    }
}

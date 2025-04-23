/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.plugin.vector.rescorer;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.opensearch.plugin.vector.util.VectorUtils;
import org.opensearch.search.rescore.Rescorer;
import org.opensearch.search.rescore.RescoreContext;
import org.opensearch.plugin.vector.rescorer.MaxSimRescorerBuilder.MaxSimRescoreContext;

import java.io.IOException;
import java.util.List;

/**
 * Rescorer implementation that computes MaxSim scores between query vectors
 * and document vectors for late interaction retrieval models.
 */
public class MaxSimRescorer implements Rescorer {

    public static final Rescorer INSTANCE = new MaxSimRescorer();

    @Override
    public TopDocs rescore(TopDocs topDocs, IndexSearcher searcher, RescoreContext context) throws IOException {
        MaxSimRescoreContext maxSimContext = (MaxSimRescoreContext) context;
        List<List<Float>> queryVectors = maxSimContext.getQueryVectors();
        String field = maxSimContext.getField();
        String similarity = maxSimContext.getSimilarity();
        
        // Make a copy of the scoreDocs array so we can modify the scores
        ScoreDoc[] scoreDocs = new ScoreDoc[topDocs.scoreDocs.length];
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            ScoreDoc original = topDocs.scoreDocs[i];
            scoreDocs[i] = new ScoreDoc(original.doc, original.score, original.shardIndex);
        }
        
        // Only rescore the top N documents based on window size
        int windowSize = Math.min(context.getWindowSize(), scoreDocs.length);

        // Get LeafReaderContext for accessing document data
        List<LeafReaderContext> leaves = searcher.getIndexReader().leaves();
        
        // Rescore each document
        for (int i = 0; i < windowSize; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            int docId = scoreDoc.doc;
            
            // Find the right segment for this document
            LeafReaderContext leaf = null;
            int leafDocId = docId;
            for (LeafReaderContext ctx : leaves) {
                if (docId >= ctx.docBase && docId < ctx.docBase + ctx.reader().maxDoc()) {
                    leaf = ctx;
                    leafDocId = docId - ctx.docBase;
                    break;
                }
            }
            
            if (leaf == null) {
                continue; // Skip if we can't find the document
            }
            
            // In a real implementation, we would retrieve document vectors here
            // For now, we'll use placeholder vectors for demonstration
            List<List<Float>> docVectors = getPlaceholderDocumentVectors(leafDocId);
            
            // Calculate MaxSim score
            float maxSimScore = VectorUtils.computeMaxSim(queryVectors, docVectors, similarity);
            
            // Combine with original score based on weight
            float weight = 1.0f; // Default weight
            if (context instanceof MaxSimRescorerBuilder.MaxSimRescoreContext) {
                weight = ((MaxSimRescorerBuilder.MaxSimRescoreContext) context).getQueryWeight();
            }
            float originalScore = scoreDoc.score;
            scoreDoc.score = (1 - weight) * originalScore + weight * maxSimScore;
        }
        
        // Create new TopDocs with rescored documents
        return new TopDocs(topDocs.totalHits, scoreDocs);
    }

    /**
     * Placeholder method that returns mock document vectors for demonstration
     * In a real implementation, this would retrieve vectors from the index
     * 
     * @param docId The document ID
     * @return List of document token vectors
     */
    private List<List<Float>> getPlaceholderDocumentVectors(int docId) {
        // In a real implementation, we would retrieve actual vectors from the document
        // For now, return placeholder vectors to demonstrate the concept
        return List.of(
            List.of(0.1f, 0.2f, 0.3f),
            List.of(0.4f, 0.5f, 0.6f),
            List.of(0.7f, 0.8f, 0.9f)
        );
    }

    @Override
    public Explanation explain(int docId, IndexSearcher searcher, RescoreContext context,
                             Explanation sourceExplanation) throws IOException {
        MaxSimRescoreContext maxSimContext = (MaxSimRescoreContext) context;
        List<List<Float>> queryVectors = maxSimContext.getQueryVectors();
        String field = maxSimContext.getField();
        String similarity = maxSimContext.getSimilarity();
        
        List<LeafReaderContext> leaves = searcher.getIndexReader().leaves();
        
        // Find the right segment for this document
        LeafReaderContext leaf = null;
        int leafDocId = docId;
        for (LeafReaderContext ctx : leaves) {
            if (docId >= ctx.docBase && docId < ctx.docBase + ctx.reader().maxDoc()) {
                leaf = ctx;
                leafDocId = docId - ctx.docBase;
                break;
            }
        }
        
        if (leaf == null) {
            return Explanation.noMatch("Document not found");
        }
        
        // Get document vectors (placeholder for now)
        List<List<Float>> docVectors = getPlaceholderDocumentVectors(leafDocId);
        
        // Calculate MaxSim score
        float maxSimScore = VectorUtils.computeMaxSim(queryVectors, docVectors, similarity);
        
        // Calculate final score with weight
        float weight = 1.0f; // Default weight
        if (context instanceof MaxSimRescorerBuilder.MaxSimRescoreContext) {
            weight = ((MaxSimRescorerBuilder.MaxSimRescoreContext) context).getQueryWeight();
        }
        float originalScore = sourceExplanation.getValue().floatValue();
        float finalScore = (1 - weight) * originalScore + weight * maxSimScore;
        
        return Explanation.match(
            finalScore,
            String.format("MaxSim rescoring: (original=%.2f * (1-weight=%.2f)) + (maxsim=%.2f * weight=%.2f)",
                         originalScore, weight, maxSimScore, weight),
            List.of(sourceExplanation, 
                    Explanation.match(maxSimScore, "MaxSim score using " + similarity + " similarity"))
        );
    }
}

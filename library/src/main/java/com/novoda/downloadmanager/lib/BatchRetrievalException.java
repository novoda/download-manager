package com.novoda.downloadmanager.lib;

final class BatchRetrievalException extends RuntimeException {
    static BatchRetrievalException failedQueryForBatches() {
        return new BatchRetrievalException("Failed to query for batches");
    }

    static BatchRetrievalException failedQueryForBatch(long batchId) {
        return new BatchRetrievalException("Failed to query for batch with batchId = " + batchId);
    }

    private BatchRetrievalException(String message) {
        super(message);
    }
}

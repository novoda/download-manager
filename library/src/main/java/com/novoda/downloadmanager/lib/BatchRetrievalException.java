package com.novoda.downloadmanager.lib;

final class BatchRetrievalException extends RuntimeException {

    BatchRetrievalException() {
        super("Failed to query for batches");
    }

    BatchRetrievalException(long batchId) {
        super("Failed to query for batch with batchId = " + batchId);
    }

}

package com.novoda.downloadmanager;

/**
 * Adds {@link BatchFile} to the parent {@link Batch} using the {@link BatchBuilder}
 * and {@link BatchFileBuilder} fluent APIs.
 */
public interface BatchFileBuilder {

    /**
     * Sets {@link BatchFileBuilder} to build a {@link BatchFile} with a corresponding
     * {@param downloadFileId} that can be used to lookup a download file belonging to
     * a given {@link Batch}.
     *
     * @param downloadFileId to flag a {@link BatchFile} with.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder withIdentifier(DownloadFileId downloadFileId);

    /**
     * Sets {@link BatchFileBuilder} to build a {@link BatchFile} that will be saved
     * to the given {@param path}.
     *
     * @param path to save file to.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder saveTo(String path);

    /**
     * Sets {@link BatchFileBuilder} to build a {@link BatchFile} that will be saved
     * to the given {@param path} along with the given {@param filename}.
     *
     * @param path     to save the file to.
     * @param fileName of the file to save.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder saveTo(String path, String fileName);

    /**
     * Creates a {@link BatchFile} from the {@link BatchFileBuilder} and
     * adds it to the parent {@link BatchBuilder} before returning to
     * continue the fluent API.
     *
     * @return an instance of {@link BatchBuilder}.
     */
    BatchBuilder apply();
}

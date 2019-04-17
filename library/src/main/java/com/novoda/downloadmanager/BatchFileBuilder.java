package com.novoda.downloadmanager;

/**
 * Adds {@link BatchFile} to the parent {@link Batch} using the {@link BatchBuilder}
 * and {@link BatchFileBuilder} fluent APIs.
 */
public interface BatchFileBuilder {

    /**
     * Sets {@link BatchFileBuilder} to build a {@link BatchFile} with a corresponding
     * identifier that can be later used to lookup a downloaded file.
     *
     * @param downloadFileId to flag a {@link BatchFile} with.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder withIdentifier(DownloadFileId downloadFileId);

    /**
     * Sets {@link BatchFileBuilder} to build a {@link BatchFile} that will be saved
     * to the given path.
     *
     * @param path directory to save the file to.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder saveTo(String path);

    /**
     * Sets {@link BatchFileBuilder} to build a {@link BatchFile} that will be saved
     * to the given path along with the given filename.
     *
     * @param path     directory to save the file to.
     * @param fileName to assign to file.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder saveTo(String path, String fileName);

    BatchFileBuilder withSize(FileSize fileSize);

    /**
     * Creates a {@link BatchFile} from the {@link BatchFileBuilder} and
     * adds it to the parent {@link BatchBuilder} before returning to
     * continue the fluent API.
     *
     * @return an instance of {@link BatchBuilder}.
     */
    BatchBuilder apply();
}

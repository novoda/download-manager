package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.DownloadsBatchPersistedFixtures.aDownloadsBatchPersisted;
import static com.novoda.downloadmanager.DownloadsFilePersistedFixtures.aDownloadsFilePersisted;

public class UnlinkedDataRemoverTest {

    private static final String LINKED_FIRST_FILENAME = "a filename";
    private static final String LINKED_SECOND_FILENAME = "another filename";
    private static final String UNLINKED_THIRD_FILE = "yet another filename";

    private final DownloadsPersistence downloadsPersistence = new DummyDownloadsPersistence();

    private final LocalFilesDirectory localFilesDirectory = new FakeLocalFilesDirectory(Arrays.asList(LINKED_FIRST_FILENAME, LINKED_SECOND_FILENAME, UNLINKED_THIRD_FILE));
    private final UnlinkedDataRemover unlinkedDataRemover = instantiateTestSubject(downloadsPersistence, localFilesDirectory);

    @Test
    public void removesFiles_whenPresentInLocalStorageButNotInV2Database() {
        unlinkedDataRemover.remove();

        List<String> expectedContents = Arrays.asList(LINKED_FIRST_FILENAME, LINKED_SECOND_FILENAME);
        List<String> actualContents = localFilesDirectory.contents();
        assertThat(actualContents).isEqualTo(expectedContents);
    }

    private static UnlinkedDataRemover instantiateTestSubject(DownloadsPersistence downloadsPersistence, LocalFilesDirectory localFilesDirectory) {
        return new UnlinkedDataRemover(downloadsPersistence, localFilesDirectory);
    }

    private static class FakeLocalFilesDirectory implements LocalFilesDirectory {

        private List<String> fileList;

        FakeLocalFilesDirectory(List<String> fileList) {
            this.fileList = new ArrayList<>(fileList);
        }

        @Override
        public List<String> contents() {
            return fileList;
        }

        @Override
        public boolean deleteFile(String filename) {
            boolean fileWasRemoved = false;
            List<String> newFileList = new ArrayList<>();
            for (String s : fileList) {
                if (filename.equals(s)) {
                    fileWasRemoved = true;
                } else {
                    newFileList.add(s);
                }
            }
            fileList = newFileList;
            return fileWasRemoved;
        }
    }

    private class DummyDownloadsPersistence implements DownloadsPersistence {

        @Override
        public void startTransaction() {

        }

        @Override
        public void endTransaction() {

        }

        @Override
        public void transactionSuccess() {

        }

        @Override
        public void persistBatch(DownloadsBatchPersisted batchPersisted) {

        }

        @Override
        public List<DownloadsBatchPersisted> loadBatches() {
            DownloadsBatchPersisted firstBatch = aDownloadsBatchPersisted().withDownloadBatchId("first batch id").withDownloadBatchTitle("first batch title").build();
            DownloadsBatchPersisted secondBatch = aDownloadsBatchPersisted().withDownloadBatchId("second batch id").withDownloadBatchTitle("second batch title").build();

            return Arrays.asList(firstBatch, secondBatch);
        }

        @Override
        public void persistFile(DownloadsFilePersisted filePersisted) {

        }

        @Override
        public List<DownloadsFilePersisted> loadFiles(final DownloadBatchId batchId) {
            DownloadsFilePersisted firstFile = aDownloadsFilePersisted().withDownloadBatchId(batchId).withFileName(LINKED_FIRST_FILENAME).build();
            DownloadsFilePersisted secondFile = aDownloadsFilePersisted().withDownloadBatchId(batchId).withFileName(LINKED_SECOND_FILENAME).build();
            return Arrays.asList(firstFile, secondFile);
        }

        @Override
        public void delete(DownloadBatchId downloadBatchId) {

        }

        @Override
        public void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {

        }
    }
}

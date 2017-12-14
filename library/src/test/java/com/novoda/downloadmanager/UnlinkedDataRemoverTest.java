package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.DownloadsBatchPersistedFixtures.aDownloadsBatchPersisted;
import static com.novoda.downloadmanager.DownloadsFilePersistedFixtures.aDownloadsFilePersisted;

public class UnlinkedDataRemoverTest {

    private static final String LINKED_FIRST_FILENAME = "a filename";
    private static final String LINKED_SECOND_FILENAME = "another filename";
    private static final String UNLINKED_THIRD_FILE = "yet another filename";

    private final DownloadsPersistence downloadsPersistence = new FakeDownloadsPersistence(createBatchesWithFiles());
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

    private Map<DownloadsBatchPersisted, List<DownloadsFilePersisted>> createBatchesWithFiles() {
        Map<DownloadsBatchPersisted, List<DownloadsFilePersisted>> batchesWithFiles = new HashMap<>();
        DownloadsBatchPersisted batch = aDownloadsBatchPersisted().withRawDownloadBatchId("a batch id").build();
        DownloadsFilePersisted firstFile = aDownloadsFilePersisted().withRawFileName(LINKED_FIRST_FILENAME).build();
        DownloadsFilePersisted secondFile = aDownloadsFilePersisted().withRawFileName(LINKED_SECOND_FILENAME).build();
        batchesWithFiles.put(batch, Arrays.asList(firstFile, secondFile));
        return batchesWithFiles;
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

}

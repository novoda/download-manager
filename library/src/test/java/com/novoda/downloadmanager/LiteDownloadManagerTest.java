package com.novoda.downloadmanager;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.DownloadBatchIdFixtures.aDownloadBatchId;
import static com.novoda.downloadmanager.DownloadFileIdFixtures.aDownloadFileId;
import static com.novoda.downloadmanager.DownloadFileStatusFixtures.aDownloadFileStatus;
import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(Enclosed.class)
public class LiteDownloadManagerTest {

    public static class ServiceDoesNotExist extends BaseTest {

        @Rule
        public Timeout timeout = new Timeout(500, TimeUnit.MILLISECONDS) {
            public Statement apply(Statement base, Description description) {
                return new FailOnTimeout(base, 500) {
                    @Override
                    public void evaluate() throws Throwable {
                        try {
                            super.evaluate();
                            throw new TimeoutException();
                        } catch (Exception ignored) {
                        }
                    }
                };
            }
        };

        @Test(expected = TimeoutException.class)
        public void waitsForServiceToExist_whenGettingAllBatchStatuses() {
            liteDownloadManager.getAllDownloadBatchStatuses(allBatchStatusesCallback);
        }

        @Test(expected = TimeoutException.class)
        public void waitsForServiceToExist_whenGettingAllBatchStatusesWithSynchronousCall() {
            liteDownloadManager.getAllDownloadBatchStatuses();
        }

        @Test(expected = TimeoutException.class)
        public void waitsForServiceToExist_whenGettingDownloadStatusWithMatchingId() {
            liteDownloadManager.getDownloadFileStatusWithMatching(DOWNLOAD_BATCH_ID, DOWNLOAD_FILE_ID, downloadFileStatusCallback);
        }

        @Test(expected = TimeoutException.class)
        public void waitsForServiceToExist_whenGettingDownloadStatusWithMatchingIdWithSynchronousCall() {
            liteDownloadManager.getDownloadFileStatusWithMatching(DOWNLOAD_BATCH_ID, DOWNLOAD_FILE_ID);
        }
    }

    public static class ServiceExists extends BaseTest {

        @Override
        public void setUp() {
            super.setUp();
            liteDownloadManager.initialise(downloadService);
        }

        @Test
        public void setsDownloadService() {
            verify(downloadManagerDownloader).setDownloadService(downloadService);
        }

        @Test
        public void triggersDownloadOfBatches_whenSubmittingAllStoredDownloads() {
            liteDownloadManager.submitAllStoredDownloads(allStoredDownloadsSubmittedCallback);

            InOrder inOrder = inOrder(downloadManagerDownloader);
            inOrder.verify(downloadManagerDownloader).download(downloadBatch, downloadingBatches);
            inOrder.verify(downloadManagerDownloader).download(additionalDownloadBatch, downloadingBatches);
        }

        @Test
        public void doesNotPutBatchesIntoDownloadsMap_whenSubmittingAllStoredDownloads() {
            downloadingBatches.clear();

            liteDownloadManager.submitAllStoredDownloads(allStoredDownloadsSubmittedCallback);

            assertThat(downloadingBatches).isEmpty();
        }

        @Test
        public void notifies_whenSubmittingAllStoredDownloads() {
            liteDownloadManager.submitAllStoredDownloads(allStoredDownloadsSubmittedCallback);

            verify(allStoredDownloadsSubmittedCallback).onAllDownloadsSubmitted();
        }

        @Test
        public void downloadGivenBatch_whenBatchIsNotAlreadyBeingDownloaded() {
            downloadingBatches.clear();

            liteDownloadManager.download(BATCH);

            verify(downloadManagerDownloader).download(BATCH, downloadingBatches);
        }

        @Test
        public void doesNotDownload_whenBatchIsAlreadyBeingDownloaded() {
            liteDownloadManager.download(BATCH);

            verify(downloadManagerDownloader, never()).download(BATCH, downloadingBatches);
        }

        @Test
        public void doesNotPause_whenBatchIdIsUnknown() {
            liteDownloadManager.pause(new LiteDownloadBatchId("unknown"));

            verifyZeroInteractions(downloadBatch, additionalDownloadBatch);
        }

        @Test
        public void pausesBatch() {
            liteDownloadManager.pause(DOWNLOAD_BATCH_ID);

            verify(downloadBatch).pause();
        }

        @Test
        public void doesNotResume_whenBatchIdIsUnknown() {
            liteDownloadManager.pause(new LiteDownloadBatchId("unknown"));

            verifyZeroInteractions(downloadBatch, additionalDownloadBatch);
        }

        @Test
        public void doesNotResume_whenBatchIsAlreadyDownloading() {
            given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADING).build());

            liteDownloadManager.resume(DOWNLOAD_BATCH_ID);

            InOrder inOrder = inOrder(downloadBatch);
            inOrder.verify(downloadBatch).status();
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        public void resumesBatch() {
            given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().build());

            liteDownloadManager.resume(DOWNLOAD_BATCH_ID);

            verify(downloadBatch).resume();
        }

        @Test
        public void triggersDownload_whenResumingBatch() {
            given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().build());

            liteDownloadManager.resume(DOWNLOAD_BATCH_ID);

            verify(downloadManagerDownloader).download(downloadBatch, downloadingBatches);
        }

        @Test
        public void doesNotDelete_whenBatchIdIsUnknown() {
            liteDownloadManager.delete(new LiteDownloadBatchId("unknown"));

            verifyZeroInteractions(downloadBatch, additionalDownloadBatch);
        }

        @Test
        public void deletesBatch() {
            liteDownloadManager.delete(DOWNLOAD_BATCH_ID);

            verify(downloadBatch).delete();
        }

        @Test
        public void removesBatchFromMap_whenDeletingBatch() {
            liteDownloadManager.delete(DOWNLOAD_BATCH_ID);

            assertThat(downloadingBatches).doesNotContainKey(DOWNLOAD_BATCH_ID);
        }

        @Test
        public void addsCallbackToInternalList() {
            DownloadBatchStatusCallback additionalDownloadBatchCallback = mock(DownloadBatchStatusCallback.class);

            liteDownloadManager.addDownloadBatchCallback(additionalDownloadBatchCallback);

            assertThat(downloadBatchCallbacks).contains(additionalDownloadBatchCallback);
        }

        @Test
        public void removesCallbackFromInternalList() {
            liteDownloadManager.removeDownloadBatchCallback(downloadBatchCallback);

            assertThat(downloadBatchCallbacks).doesNotContain(downloadBatchCallback);
        }

        @Test
        public void getsAllBatchStatuses_whenServiceAlreadyExists() {
            liteDownloadManager.getAllDownloadBatchStatuses(allBatchStatusesCallback);

            assertThat(downloadBatchStatuses).containsExactly(BATCH_STATUS, ADDITIONAL_BATCH_STATUS);
        }

        @Test
        public void getsAllBatchStatusesWithSynchronousCall_whenServiceAlreadyExists() {
            List<DownloadBatchStatus> allDownloadBatchStatuses = liteDownloadManager.getAllDownloadBatchStatuses();

            assertThat(allDownloadBatchStatuses).containsExactly(BATCH_STATUS, ADDITIONAL_BATCH_STATUS);
        }

        @Test
        public void getsDownloadStatusMatchingId_whenServiceAlreadyExists() {
            liteDownloadManager.getDownloadFileStatusWithMatching(DOWNLOAD_BATCH_ID, DOWNLOAD_FILE_ID, downloadFileStatusCallback);

            assertThat(downloadFileStatus).isEqualTo(DOWNLOAD_FILE_STATUS);
        }

        @Test
        public void getsDownloadStatusMatchingIdWithSynchronousCall_whenServiceAlreadyExists() {
            DownloadFileStatus fileStatus = liteDownloadManager.getDownloadFileStatusWithMatching(DOWNLOAD_BATCH_ID, DOWNLOAD_FILE_ID);

            assertThat(fileStatus).isEqualTo(DOWNLOAD_FILE_STATUS);
        }

        @Test
        public void updateAllowedConnectionTypeInConnectionChecker_whenUpdatedInDownloadManager() {
            liteDownloadManager.updateAllowedConnectionType(ANY_CONNECTION_TYPE);

            verify(connectionChecker).updateAllowedConnectionType(ANY_CONNECTION_TYPE);
        }

        @Test
        public void stopFileDownloader_whenUpdatedInDownloadManager_andConnectionTypeNotAllowed() {
            given(connectionChecker.isAllowedToDownload()).willReturn(false);

            liteDownloadManager.updateAllowedConnectionType(ANY_CONNECTION_TYPE);

            for (DownloadBatch batch : downloadingBatches.values()) {
                verify(batch).waitForNetwork();
            }
        }

        @Test(expected = IllegalArgumentException.class)
        public void throwException_whenUpdatedWithNullConnectionType() {
            liteDownloadManager.updateAllowedConnectionType(null);
        }
    }

    public abstract static class BaseTest {
        static final InternalDownloadBatchStatus BATCH_STATUS = anInternalDownloadsBatchStatus().build();
        static final InternalDownloadBatchStatus ADDITIONAL_BATCH_STATUS = anInternalDownloadsBatchStatus().build();
        static final DownloadBatchId DOWNLOAD_BATCH_ID = aDownloadBatchId().withRawDownloadBatchId("id01").build();
        static final DownloadBatchId ADDITIONAL_DOWNLOAD_BATCH_ID = aDownloadBatchId().withRawDownloadBatchId("id02").build();
        static final Batch BATCH = Batch.with(TestStorageRootFactory.create(), DOWNLOAD_BATCH_ID, "title").build();
        static final DownloadFileId DOWNLOAD_FILE_ID = aDownloadFileId().withRawDownloadFileId("file_id_01").build();
        static final DownloadFileStatus DOWNLOAD_FILE_STATUS = aDownloadFileStatus().withDownloadFileId(DOWNLOAD_FILE_ID).build();
        static final ConnectionType ANY_CONNECTION_TYPE = ConnectionType.METERED;

        final AllStoredDownloadsSubmittedCallback allStoredDownloadsSubmittedCallback = mock(AllStoredDownloadsSubmittedCallback.class);
        final AllBatchStatusesCallback allBatchStatusesCallback = mock(AllBatchStatusesCallback.class);
        final DownloadFileStatusCallback downloadFileStatusCallback = mock(DownloadFileStatusCallback.class);
        final DownloadService downloadService = mock(DownloadService.class);
        final Object serviceLock = spy(new Object());
        final Object callbackLock = spy(new Object());
        final ExecutorService executorService = mock(ExecutorService.class);
        final Handler handler = mock(Handler.class);
        final DownloadBatch downloadBatch = mock(DownloadBatch.class);
        final DownloadBatch additionalDownloadBatch = mock(DownloadBatch.class);
        final DownloadBatchStatusCallback downloadBatchCallback = mock(DownloadBatchStatusCallback.class);
        final FileOperations fileOperations = mock(FileOperations.class);
        final FileDownloader fileDownloader = mock(FileDownloader.class);
        final FileDownloaderCreator fileDownloaderCreator = mock(FileDownloaderCreator.class);
        final DownloadsBatchPersistence downloadsBatchPersistence = mock(DownloadsBatchPersistence.class);
        final LiteDownloadManagerDownloader downloadManagerDownloader = mock(LiteDownloadManagerDownloader.class);
        final ConnectionChecker connectionChecker = mock(ConnectionChecker.class);
        final Wait.Criteria serviceCriteria = new Wait.Criteria();

        LiteDownloadManager liteDownloadManager;
        Map<DownloadBatchId, DownloadBatch> downloadingBatches = new HashMap<>();
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>();
        Set<DownloadBatchStatusCallback> downloadBatchCallbacks = new CopyOnWriteArraySet<>();
        DownloadFileStatus downloadFileStatus = null;

        @Before
        public void setUp() {
            downloadingBatches = new HashMap<>();
            downloadingBatches.put(DOWNLOAD_BATCH_ID, downloadBatch);
            downloadingBatches.put(ADDITIONAL_DOWNLOAD_BATCH_ID, additionalDownloadBatch);

            downloadBatchCallbacks.add(downloadBatchCallback);

            liteDownloadManager = new LiteDownloadManager(
                    serviceLock,
                    callbackLock,
                    executorService,
                    handler,
                    downloadingBatches,
                    downloadBatchCallbacks,
                    fileOperations,
                    downloadsBatchPersistence,
                    downloadManagerDownloader,
                    connectionChecker,
                    serviceCriteria
            );

            setupDownloadBatchesResponse();
            setupDownloadBatchStatusesResponse();
            setupDownloadStatusResponse();
            setupNetworkRecoveryCreator();
            setupFileOperations();

            given(downloadBatch.status()).willReturn(BATCH_STATUS);
            given(downloadBatch.downloadFileStatusWith(DOWNLOAD_FILE_ID)).willReturn(DOWNLOAD_FILE_STATUS);
            given(additionalDownloadBatch.downloadFileStatusWith(DOWNLOAD_FILE_ID)).willReturn(DOWNLOAD_FILE_STATUS);
            given(additionalDownloadBatch.status()).willReturn(ADDITIONAL_BATCH_STATUS);

            willAnswer(invocation -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }).given(executorService).submit(any(Runnable.class));

            willAnswer(invocation -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }).given(handler).post(any(Runnable.class));
        }

        private void setupDownloadBatchesResponse() {
            willAnswer(invocation -> {
                DownloadsBatchPersistence.LoadBatchesCallback loadBatchesCallback = invocation.getArgument(1);
                loadBatchesCallback.onLoaded(Arrays.asList(downloadBatch, additionalDownloadBatch));
                return null;
            }).given(downloadsBatchPersistence).loadAsync(any(FileOperations.class), any(DownloadsBatchPersistence.LoadBatchesCallback.class));
        }

        private void setupDownloadBatchStatusesResponse() {
            willAnswer(invocation -> {
                downloadBatchStatuses = invocation.getArgument(0);
                return null;
            }).given(allBatchStatusesCallback).onReceived(ArgumentMatchers.anyList());
        }

        private void setupDownloadStatusResponse() {
            willAnswer(invocation -> {
                downloadFileStatus = invocation.getArgument(0);
                return null;
            }).given(downloadFileStatusCallback).onReceived(any(DownloadFileStatus.class));
        }

        private void setupNetworkRecoveryCreator() {
            DownloadsNetworkRecoveryCreator.createDisabled();
        }

        private void setupFileOperations() {
            given(fileOperations.fileDownloaderCreator()).willReturn(fileDownloaderCreator);
            given(fileDownloaderCreator.create()).willReturn(fileDownloader);
        }
    }
}

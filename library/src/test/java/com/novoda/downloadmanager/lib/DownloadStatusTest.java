package com.novoda.downloadmanager.lib;

import android.app.DownloadManager;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DownloadStatusTest {

    @Test
    public void whenVisibilityIsCompletedThenNotificationIsToBeDisplayed() {
        boolean toBeDisplayed = DownloadStatus.isNotificationToBeDisplayed(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        assertThat(toBeDisplayed).isTrue();
    }

    @Test
    public void whenVisibilityIsOnlyCompletionThenNotificationIsToBeDisplayed() {
        boolean toBeDisplayed = DownloadStatus.isNotificationToBeDisplayed(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
        assertThat(toBeDisplayed).isTrue();
    }

    @Test
    public void whenVisibilityIsHiddenThenNotificationIsNotBeDisplayed() {
        boolean toBeDisplayed = DownloadStatus.isNotificationToBeDisplayed(DownloadManager.Request.VISIBILITY_HIDDEN);
        assertThat(toBeDisplayed).isFalse();
    }

    @Test
    public void whenStatusIs490ThenStatusIsCancelled() {
        boolean isCancelled = DownloadStatus.isCancelled(490);
        assertThat(isCancelled).isTrue();
    }

    @Test
    public void whenStatusIs4xxThenStatusIsClientError() {
        for (int i = 400; i < 500; i++) {
            assertThat(DownloadStatus.isClientError(i)).isTrue();
        }

        assertThat(DownloadStatus.isClientError(399)).isFalse();
        assertThat(DownloadStatus.isClientError(500)).isFalse();
    }

    @Test
    public void whenStatusIs4xxOr5xxxThenStatusIsError() {
        for (int i = 400; i < 600; i++) {
            assertThat(DownloadStatus.isError(i)).isTrue();
        }

        assertThat(DownloadStatus.isError(399)).isFalse();
        assertThat(DownloadStatus.isError(700)).isFalse();
    }

    @Test
    public void whenStatusIs198ThenStatusIsInsufficientSpace() {
        boolean isInsufficientSpace = DownloadStatus.isCancelled(490);
        assertThat(isInsufficientSpace).isTrue();
    }

    @Test
    public void whenStatusIs5xxThenStatusIsServerError() {
        for (int i = 500; i < 600; i++) {
            assertThat(DownloadStatus.isServerError(i)).isTrue();
        }

        assertThat(DownloadStatus.isServerError(499)).isFalse();
        assertThat(DownloadStatus.isServerError(600)).isFalse();
    }

    @Test
    public void whenStatusIs1xxThenStatusIsInformational() {
        for (int i = 100; i < 200; i++) {
            assertThat(DownloadStatus.isInformational(i)).isTrue();
        }

        assertThat(DownloadStatus.isInformational(99)).isFalse();
        assertThat(DownloadStatus.isInformational(200)).isFalse();
    }

    @Test
    public void whenStatusIs2xxThenStatusIsSuccess() {
        for (int i = 200; i < 300; i++) {
            assertThat(DownloadStatus.isSuccess(i)).isTrue();
        }

        assertThat(DownloadStatus.isSuccess(199)).isFalse();
        assertThat(DownloadStatus.isSuccess(300)).isFalse();
    }

    @Test
    public void whenStatusIs2xxThenStatusIsCompleted() {
        for (int i = 200; i < 300; i++) {
            assertThat(DownloadStatus.isCompleted(i)).isTrue();
        }

        assertThat(DownloadStatus.isSuccess(199)).isFalse();
        assertThat(DownloadStatus.isSuccess(300)).isFalse();
    }

    @Test
    public void whenStatusIs4xxAnd5xxButNot490ThenStatusIsCompleted() {
        for (int i = 400; i < 600; i++) {
            if (i == 490) {
                assertThat(DownloadStatus.isCompleted(i)).isFalse();
            } else {
                assertThat(DownloadStatus.isCompleted(i)).isTrue();
            }
        }

        assertThat(DownloadStatus.isSuccess(399)).isFalse();
        assertThat(DownloadStatus.isSuccess(600)).isFalse();
    }

    @Test
    public void whenStatusIs189ThenStatusIsSubmitted() {
        boolean isStatusSubmitted = DownloadStatus.isSubmitted(189);
        assertThat(isStatusSubmitted).isTrue();
    }

    @Test
    public void whenStatusIs192ThenStatusIsRunning() {
        boolean isStatusRunning = DownloadStatus.isRunning(192);
        assertThat(isStatusRunning).isTrue();
    }

    @Test
    public void whenStatusIs188ThenStatusIsDeleting() {
        boolean isStatusDeleting = DownloadStatus.isDeleting(188);
        assertThat(isStatusDeleting).isTrue();
    }

}

package com.novoda.downloadmanager.lib;

import android.app.DownloadManager;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DownloadsStatusTest {

    @Test
    public void whenVisibilityIsCompletedThenNotificationIsToBeDisplayed() {
        boolean toBeDisplayed = DownloadsStatus.isNotificationToBeDisplayed(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        assertThat(toBeDisplayed).isTrue();
    }

    @Test
    public void whenVisibilityIsOnlyCompletionThenNotificationIsToBeDisplayed() {
        boolean toBeDisplayed = DownloadsStatus.isNotificationToBeDisplayed(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
        assertThat(toBeDisplayed).isTrue();
    }

    @Test
    public void whenVisibilityIsHiddenThenNotificationIsNotBeDisplayed() {
        boolean toBeDisplayed = DownloadsStatus.isNotificationToBeDisplayed(DownloadManager.Request.VISIBILITY_HIDDEN);
        assertThat(toBeDisplayed).isFalse();
    }

    @Test
    public void whenStatusIs490ThenStatusIsCancelled() {
        boolean isCancelled = DownloadsStatus.isStatusCancelled(490);
        assertThat(isCancelled).isTrue();
    }

    @Test
    public void whenStatusIs4xxThenStatusIsClientError() {
        for (int i = 400; i < 500; i++) {
            assertThat(DownloadsStatus.isStatusClientError(i)).isTrue();
        }

        assertThat(DownloadsStatus.isStatusClientError(399)).isFalse();
        assertThat(DownloadsStatus.isStatusClientError(500)).isFalse();
    }

    @Test
    public void whenStatusIs4xxOr5xxxThenStatusIsError() {
        for (int i = 400; i < 600; i++) {
            assertThat(DownloadsStatus.isStatusError(i)).isTrue();
        }

        assertThat(DownloadsStatus.isStatusError(399)).isFalse();
        assertThat(DownloadsStatus.isStatusError(700)).isFalse();
    }

    @Test
    public void whenStatusIs198ThenStatusIsInsufficientSpace() {
        boolean isInsufficientSpace = DownloadsStatus.isStatusCancelled(490);
        assertThat(isInsufficientSpace).isTrue();
    }

    @Test
    public void whenStatusIs5xxThenStatusIsServerError() {
        for (int i = 500; i < 600; i++) {
            assertThat(DownloadsStatus.isStatusServerError(i)).isTrue();
        }

        assertThat(DownloadsStatus.isStatusServerError(499)).isFalse();
        assertThat(DownloadsStatus.isStatusServerError(600)).isFalse();
    }

    @Test
    public void whenStatusIs1xxThenStatusIsInformational() {
        for (int i = 100; i < 200; i++) {
            assertThat(DownloadsStatus.isStatusInformational(i)).isTrue();
        }

        assertThat(DownloadsStatus.isStatusInformational(99)).isFalse();
        assertThat(DownloadsStatus.isStatusInformational(200)).isFalse();
    }

    @Test
    public void whenStatusIs2xxThenStatusIsSuccess() {
        for (int i = 200; i < 300; i++) {
            assertThat(DownloadsStatus.isStatusSuccess(i)).isTrue();
        }

        assertThat(DownloadsStatus.isStatusSuccess(199)).isFalse();
        assertThat(DownloadsStatus.isStatusSuccess(300)).isFalse();
    }

    @Test
    public void whenStatusIs2xxThenStatusIsCompleted() {
        for (int i = 200; i < 300; i++) {
            assertThat(DownloadsStatus.isStatusCompleted(i)).isTrue();
        }

        assertThat(DownloadsStatus.isStatusSuccess(199)).isFalse();
        assertThat(DownloadsStatus.isStatusSuccess(300)).isFalse();
    }

    @Test
    public void whenStatusIs4xxAnd5xxButNot490ThenStatusIsCompleted() {
        for (int i = 400; i < 600; i++) {
            if (i == 490) {
                assertThat(DownloadsStatus.isStatusCompleted(i)).isFalse();
            } else {
                assertThat(DownloadsStatus.isStatusCompleted(i)).isTrue();
            }
        }

        assertThat(DownloadsStatus.isStatusSuccess(399)).isFalse();
        assertThat(DownloadsStatus.isStatusSuccess(600)).isFalse();
    }

}

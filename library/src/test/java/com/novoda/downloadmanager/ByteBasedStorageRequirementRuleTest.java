package com.novoda.downloadmanager;

import java.io.File;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ByteBasedStorageRequirementRuleTest {

    private static final long CAPACITY_ONE_GB_IN_BYTES = 1000000000;
    private static final long USABLE_THREE_HUNDRED_MB_IN_BYTES = 300000000;
    private static final long REMAINING_OVER_ONE_HUNDRED_MB_IN_BYTES = 100000001;
    private static final long REMAINING_ONE_HUNDRED_MB_IN_BYTES = 100000000;

    private static final long TWO_HUNDRED_MB_IN_BYTES_REMAINING = 200000000;

    private final File file = createFile();
    private final StorageCapacityReader storageCapacityReader = createStorageCapacityReader();
    private final ByteBasedStorageRequirementRule storageRequirementRule = new ByteBasedStorageRequirementRule(storageCapacityReader, TWO_HUNDRED_MB_IN_BYTES_REMAINING);

    @Test
    public void doesNotViolateRule_whenRemainingFileSizeIsLessThanRestriction() {
        FileSize fileSize = new RemainingFileSize(REMAINING_ONE_HUNDRED_MB_IN_BYTES);

        boolean hasViolatedRule = storageRequirementRule.hasViolatedRule(file, fileSize);

        assertThat(hasViolatedRule).isFalse();
    }

    @Test
    public void violatesRule_whenRemainingFileSizeIsGreaterThanRestriction() {
        FileSize fileSize = new RemainingFileSize(REMAINING_OVER_ONE_HUNDRED_MB_IN_BYTES);

        boolean hasViolatedRule = storageRequirementRule.hasViolatedRule(file, fileSize);

        assertThat(hasViolatedRule).isTrue();
    }

    private static File createFile() {
        File file = mock(File.class);
        given(file.getUsableSpace()).willReturn(USABLE_THREE_HUNDRED_MB_IN_BYTES);
        return file;
    }

    private static StorageCapacityReader createStorageCapacityReader() {
        StorageCapacityReader storageCapacityReader = mock(StorageCapacityReader.class);
        given(storageCapacityReader.storageCapacityInBytes(anyString())).willReturn(CAPACITY_ONE_GB_IN_BYTES);
        return storageCapacityReader;
    }

    private class RemainingFileSize implements FileSize {

        private final long remainingSizeInBytes;

        RemainingFileSize(long remainingSizeInBytes) {
            this.remainingSizeInBytes = remainingSizeInBytes;
        }

        @Override
        public long currentSize() {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public long totalSize() {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public long remainingSize() {
            return remainingSizeInBytes;
        }

        @Override
        public boolean isTotalSizeKnown() {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public boolean isTotalSizeUnknown() {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public boolean areBytesDownloadedKnown() {
            throw new IllegalStateException("not implemented");
        }
    }
}

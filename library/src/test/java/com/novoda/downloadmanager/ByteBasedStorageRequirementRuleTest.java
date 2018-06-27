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

    private final FileSize fileSize = mock(FileSize.class);
    private final File file = createFile();
    private final StorageCapacityReader storageCapacityReader = createStorageCapacityReader();
    private final ByteBasedStorageRequirementRule storageRequirementRule = new ByteBasedStorageRequirementRule(storageCapacityReader, TWO_HUNDRED_MB_IN_BYTES_REMAINING);

    @Test
    public void doesNotViolateRule_whenRemainingFileSizeIsLessThanRestriction() {
        given(fileSize.remainingSize()).willReturn(REMAINING_ONE_HUNDRED_MB_IN_BYTES);

        boolean hasViolatedRule = storageRequirementRule.hasViolatedRule(file, fileSize);

        assertThat(hasViolatedRule).isFalse();
    }

    @Test
    public void violatesRule_whenRemainingFileSizeIsGreaterThanRestriction() {
        given(fileSize.remainingSize()).willReturn(REMAINING_OVER_ONE_HUNDRED_MB_IN_BYTES);

        boolean hasViolatedRule = storageRequirementRule.hasViolatedRule(file, fileSize);

        assertThat(hasViolatedRule).isTrue();
    }

    private static File createFile() {
        File file = mock(File.class);
        given(file.getPath()).willReturn("any_path");
        given(file.getUsableSpace()).willReturn(USABLE_THREE_HUNDRED_MB_IN_BYTES);
        return file;
    }

    private static StorageCapacityReader createStorageCapacityReader() {
        StorageCapacityReader storageCapacityReader = mock(StorageCapacityReader.class);
        given(storageCapacityReader.storageCapacityInBytes(anyString())).willReturn(CAPACITY_ONE_GB_IN_BYTES);
        return storageCapacityReader;
    }
}

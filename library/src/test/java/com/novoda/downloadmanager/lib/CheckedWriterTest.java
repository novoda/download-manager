package com.novoda.downloadmanager.lib;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class CheckedWriterTest {

    private static final int WRITER_OFFSET = 0;

    @Mock
    SpaceVerifier spaceVerifier;
    @Mock
    OutputStream outputStream;

    private int count;
    private long initialCurrentBytes;
    private DownloadTask.State state;
    private byte[] buffer;

    private CheckedWriter checkedWriter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        checkedWriter = new CheckedWriter(spaceVerifier, outputStream);
    }

    @Test
    public void itWritesToTheOutputStream() throws Exception {
        givenANormalStateForACopy();

        checkedWriter.write(state, buffer, count);

        verify(outputStream).write(buffer, WRITER_OFFSET, count);
    }

    @Test
    public void gotDataIsSetToTrueAfterReceivingData() throws Exception {
        givenANormalStateForACopy();

        checkedWriter.write(state, buffer, count);

        assertThat(state.gotData).isTrue();
    }

    @Test
    public void currentBytesIsEqualToTotalBytesAfterReceivingData() throws Exception {
        givenANormalStateForACopy();

        checkedWriter.write(state, buffer, count);

        assertThat(state.currentBytes).isEqualTo(count + initialCurrentBytes);
    }

    @Test
    public void itShouldVerifySpaceIfIOException() throws Exception {
        givenAnIOExceptionOccurs();

        checkedWriter.write(state, buffer, count);

        verify(outputStream, times(2)).write(buffer, WRITER_OFFSET, count);
        verify(spaceVerifier).verifySpace(count);
    }

    private void givenANormalStateForACopy() {
        state = new DownloadTask.State();
        count = 42;
        initialCurrentBytes = state.currentBytes;
        buffer = new byte[]{};
    }

    private void givenAnIOExceptionOccurs() throws IOException {
        givenANormalStateForACopy();
        doThrow(new IOException())
                .doNothing()
                .when(outputStream)
                .write(buffer, WRITER_OFFSET, count);
    }

}

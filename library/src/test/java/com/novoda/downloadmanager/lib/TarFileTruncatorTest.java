package com.novoda.downloadmanager.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;

import static com.novoda.downloadmanager.lib.IOHelpers.closeAfterWrite;
import static com.novoda.downloadmanager.lib.IOHelpers.closeQuietly;
import static org.fest.assertions.api.Assertions.assertThat;

public class TarFileTruncatorTest {

    private TarFileTruncator tarFileTruncator;
    private InputStream resourceAsStream;
    private FileOutputStream fileOutputStream;
    private DownloadThread.State state;

    @Test
    public void itTruncatesProperlyTarFileThatContainsEndBlockMarker() throws Exception {
        givenATarFileWithEndBlockMarker();

        state = tarFileTruncator.transferData(state, resourceAsStream);

        tarFileShouldHaveBeenTruncatedProperly();
    }

    @Test
    public void itDoesNotModifyATarFileWithoutEndBlockMarker() throws Exception {
        givenATarFileWithoutEndBlockMarker();

        state = tarFileTruncator.transferData(state, resourceAsStream);

        tarFileShouldBeUntouched();
    }

    @After
    public void tearDown() throws Exception {
        new File("TarFileTruncatorTest.tar").delete();
    }

    private void givenATarFileWithEndBlockMarker() throws IOException {
        resourceAsStream = getResourceAsStream("tar/testOriginal.tar");
        fileOutputStream = new FileOutputStream("TarFileTruncatorTest.tar");
        tarFileTruncator = new TarFileTruncator(new TestDataWriter(fileOutputStream));
        state = new DownloadThread.State();
    }

    private void givenATarFileWithoutEndBlockMarker() throws IOException {
        resourceAsStream = getResourceAsStream("tar/testExpectedResult.tar");
        fileOutputStream = new FileOutputStream("TarFileTruncatorTest.tar");
        tarFileTruncator = new TarFileTruncator(new TestDataWriter(fileOutputStream));
        state = new DownloadThread.State();
    }

    private void tarFileShouldHaveBeenTruncatedProperly() throws IOException {
        closeAfterWrite(fileOutputStream, fileOutputStream.getFD());
        closeQuietly(resourceAsStream);
        boolean contentEquals = IOUtils.contentEquals(new FileInputStream("TarFileTruncatorTest.tar"), getResourceAsStream("tar/testExpectedResult.tar"));
        assertThat(contentEquals).isTrue();
    }

    private void tarFileShouldBeUntouched() throws IOException {
        closeAfterWrite(fileOutputStream, fileOutputStream.getFD());
        closeQuietly(resourceAsStream);
        boolean contentEquals = IOUtils.contentEquals(new FileInputStream("TarFileTruncatorTest.tar"), getResourceAsStream("tar/testExpectedResult.tar"));
        assertThat(contentEquals).isTrue();
    }

    private static InputStream getResourceAsStream(String resName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resName);
    }

    private static class TestDataWriter implements DataWriter {

        private final OutputStream outputStream;

        public TestDataWriter(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public DownloadThread.State write(DownloadThread.State state, byte[] buffer, int count) throws StopRequestException {
            try {
                state.gotData = true;
                outputStream.write(buffer, 0, count);
                state.currentBytes += count;
                return state;
            } catch (IOException e) {
                throw new StopRequestException(
                        DownloadStatus.FILE_ERROR,
                        "Failed to write data: " + e);
            }
        }
    }
}

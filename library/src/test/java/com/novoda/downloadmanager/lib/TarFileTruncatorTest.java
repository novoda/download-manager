package com.novoda.downloadmanager.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.novoda.downloadmanager.lib.IOHelpers.closeAfterWrite;
import static com.novoda.downloadmanager.lib.IOHelpers.closeQuietly;
import static org.fest.assertions.api.Assertions.assertThat;

public class TarFileTruncatorTest {

    private TarFileTruncator tarFileTruncator;

    @Before
    public void setUp() throws Exception {
        tarFileTruncator = new TarFileTruncator();
    }

    @Test
    public void itTruncatesProperlyTarFileThatContainsEndBlockMarker() throws Exception {
        givenATarFileWithEndBlockMarker();

        tarFileTruncator.truncateIfNeeded("TarFileTruncatorTest.tar");

        tarFileShouldHaveBeenTruncatedProperly();
    }

    @Test
    public void itDoeNotModifyATarFileWithoutEndBlockMarker() throws Exception {
        givenATarFileWithoutEndBlockMarker();

        tarFileTruncator.truncateIfNeeded("TarFileTruncatorTest.tar");

        tarFileShouldBeUntouched();
    }

    @After
    public void tearDown() throws Exception {
        new File("TarFileTruncatorTest.tar").delete();
    }

    private static void givenATarFileWithEndBlockMarker() throws IOException {
        InputStream resourceAsStream = getResourceAsStream("tar/testOriginal.tar");
        FileOutputStream fileOutputStream = new FileOutputStream("TarFileTruncatorTest.tar");
        IOUtils.copy(resourceAsStream, fileOutputStream);
        closeAfterWrite(fileOutputStream, fileOutputStream.getFD());
        closeQuietly(resourceAsStream);
    }

    private static void givenATarFileWithoutEndBlockMarker() throws IOException {
        InputStream resourceAsStream = getResourceAsStream("tar/testExpectedResult.tar");
        FileOutputStream fileOutputStream = new FileOutputStream("TarFileTruncatorTest.tar");
        IOUtils.copy(resourceAsStream, fileOutputStream);
        closeAfterWrite(fileOutputStream, fileOutputStream.getFD());
        closeQuietly(resourceAsStream);
    }

    private static void tarFileShouldHaveBeenTruncatedProperly() throws IOException {
        boolean contentEquals = IOUtils.contentEquals(new FileInputStream("TarFileTruncatorTest.tar"), getResourceAsStream("tar/testExpectedResult.tar"));
        assertThat(contentEquals).isTrue();
    }

    private static void tarFileShouldBeUntouched() throws IOException {
        boolean contentEquals = IOUtils.contentEquals(new FileInputStream("TarFileTruncatorTest.tar"), getResourceAsStream("tar/testExpectedResult.tar"));
        assertThat(contentEquals).isTrue();
    }

    private static InputStream getResourceAsStream(String resName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resName);
    }

}

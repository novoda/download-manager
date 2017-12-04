package com.novoda.downloadmanager;

import android.database.Cursor;

public class MigrationExtractorTest {

    private Cursor createStubCursor() {
        return new StubCursor.Builder()
                .withRowValues("_id", "1", "2")
                .withRowValues("batch_title", "title_1", "title_2")
                .build();
    }

}

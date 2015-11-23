CREATE TABLE IF NOT EXISTS 'request' (
                                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                request_timestamp TEXT NOT NULL,
                                UNIQUE (request_timestamp) ON CONFLICT REPLACE
);

CREATE TABLE IF NOT EXISTS 'download' (
                                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                                download_status TEXT NOT NULL,
                                download_id INTEGER NOT NULL,
                                UNIQUE (download_id) ON CONFLICT REPLACE
);

CREATE TABLE IF NOT EXISTS 'file' (
                                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                                file_status TEXT NOT NULL,
                                file_total_size INTEGER NOT NULL DEFAULT -1,
                                file_current_size INTEGER NOT NULL DEFAULT 0,
                                file_download_id INTEGER NOT NULL,
                                file_uri TEXT NOT_NULL,
                                file_local_uri TEXT NOT_NULL,
                                file_identifier TEXT NOT NULL,
                                UNIQUE (file_uri, file_identifier) ON CONFLICT REPLACE
);

CREATE VIEW IF NOT EXISTS "downloads_with_size" AS
                                SELECT
                                    SUM(file.file_current_size) as current_size,
                                    SUM(file.file_total_size) as total_size,
                                    download._id,
                                    download_status,
                                    download_id
                                    FROM file
                                LEFT OUTER JOIN
                                    download ON file.file_download_id = download.download_id;


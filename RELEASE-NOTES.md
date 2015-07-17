0.2.4
-------

- Simplify query (pull out status and control) #111
- Better support for localization #112
- Add EXTRA_DATA column to Batches table #114
- Add LAST_MODIFIED column to Batches table #117

0.2.3
-------

- Batch sizes view #105
- Notify batch modified after all downloads #109

0.2.2
-------

- flag to allow any file resume #108
- fixes to the instrumentation test build #99

0.2.1
-------

- Adds the ability to listen to changes to status changes only #97

0.2.0
-------

- download checks against the current download #89
- simplified download info object #92
- fix for no icon being set for the notification #95
- adds ability to query for extra data (api breaking!) #96

0.1.9
-------

- Adds new DELETING status #94

0.1.8
-------

- Query for batches #79
- Fix current size for batches #87
- Adds an extra column so you can pass data through the DLM #88

0.1.7
-------

- improves sort for live items #86
- update tests and injection #83

0.1.6
-------

- fix pause and resume #82
- performance improvements around download checks #84

0.1.5
-------

- adds ability to sort queries by liveness #74
- calculate entire batch size #75

0.1.4
-------

- Code cleanup #63
- Limit client knowledge of internals #59
- Adds static analysis #72

0.1.3
-------

- Incorrect DownloadBatch passed to rule checker #71
- splits demos #69
- batch id query error #67
- batch deletion bug #62
- replaces data structures #60

0.1.2
-------

- Fix bug batch deletion

0.1.1
-------

- Adds download batch pausing and resuming
- Adds download batch deletion
- Adds current and total size to batches

0.1.0
-------

- Broadcast batch completion events #47
- Optimisation - Get full batch size in one query #49
- Optimisation - Removes unnecessary full loop #50

0.0.14
-------

Adds the CollatedDownloadInfo to the downloadrulecheck (means you can check the total size of the batch in the can download check!)

0.0.13
-------

BUG FIX - Queues all downloading tasks when the downloads cannot continue

0.0.12
-------

Now allows power users to override the default database filename.

0.0.9
-------

Now allows clients to have a say if a download should go ahead or not. See #29 You hook into this through your Application class.

0.0.8
-------

Enforces passing a Context via the DownloadManagerBuilder this allows Requests created later on to not need to pass Context as a parameter. (#28)
It will also allow us to make further refactorings behind the scenes now we haz your Context bwahaha.
THIS IS AN API BREAKING CHANGE FOR DownloadManagerBuilder, DownloadManager and Request (see https://thechive.files.wordpress.com/2013/01/gifs_201.gif)

0.0.7
-------

Adds the ability to configure the maximum concurrent downloads (see #23)
Improves performance of notification image fetching (#27)

0.0.6
-------

PERFORMANCE FIX - removes verbose logging by default, this stops extra queries and string concat's that caused unneccessary GC
You can now set verbose logging on using the new `DownloadManagerBuilder.withVerboseLogging()` it is off by default. (see #20)

0.0.5
-------

Removes unused constants & minimises our public API, now we can refactor internally more safely

0.0.4
-------

Prefixes all resources with `dl__` , fixes but where the `app_name` for some clients would be "DownloadManager"

0.0.3
-------

Exposes the `DownloadManager.CONTENT_URI` so clients can query however they want (for example Loaders)

0.0.2
-------

Allows selection of content provider authority using `manifestPlaceholders = [downloadAuthority: "${applicationId}"]` and class `com.novoda.downloadmanager.Authority`

0.0.1
-------

Initial release - do not use - you will have issues with conflicting content providers if you use in more than one app (and anyone elses apps)

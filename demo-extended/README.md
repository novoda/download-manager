# download-manager [![](https://ci.novoda.com/buildStatus/icon?job=download-manager)](https://ci.novoda.com/job/download-manager/lastBuild/console) [![](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)

This is a copy of the http://developer.android.com/reference/android/app/DownloadManager.html but it allows for downloading to private internal storage.


## Demo explanation

Here we show the following use cases

   - Downloading items **serially** from a url (this means the second will not start till the first completes)
   - Querying the download manager for what has been downloaded
   - Client has rules that determine if downloads should start / resume
   - Changing the database name where all downloads are saved
   - Enqueuing batched requests
   - Querying batch information

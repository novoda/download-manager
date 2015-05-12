# download-manager [![](https://ci.novoda.com/buildStatus/icon?job=download-manager)](https://ci.novoda.com/job/download-manager/lastBuild/console) [![](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)

This is a copy of the http://developer.android.com/reference/android/app/DownloadManager.html but it allows for downloading to private internal storage.

## Description

>The download manager is a system service that handles long-running HTTP downloads. Clients may request that a URI be downloaded to a particular destination file. The download manager will conduct the download in the background, taking care of HTTP interactions and retrying downloads after failures or across connectivity changes and system reboots. Instances of this class should be obtained through getSystemService(String) by passing DOWNLOAD_SERVICE. Apps that request downloads through this API should register a broadcast receiver for ACTION_NOTIFICATION_CLICKED to appropriately handle when the user clicks on a running download in a notification or from the downloads UI. Note that the application must have the INTERNET permission to use this class.

## Adding to your project

To start using this library, add these lines to the `build.gradle` of your project:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.novoda:download-manager:0.0.3'
}
```


## Simple usage

Simple example source code can be found in this demo module: [Android Simple Demo][2]

You will need to add this to your Android application build.gradle:

```groovy

android { 
  defaultConfig {
       // other things
       
       manifestPlaceholders = [downloadAuthority: "com.your.unique.authority"]
  }
}
```
You also need to **create this file in this exact package** (we hope to do this for you in the future):
```java
package com.novoda.downloadmanager;

public class Authority {
    public static final String AUTHORITY = "com.your.unique.authority";
}
```


## Links

Here are a list of useful links:

 * We always welcome people to contribute new features or bug fixes, [here is how](https://github.com/novoda/novoda/blob/master/CONTRIBUTING.md)
 * If you have a problem check the [Issues Page](https://github.com/novoda/download-manager/issues) first to see if we are working on it
 * For further usage or to delve more deeply checkout the [Project Wiki](https://github.com/novoda/download-manager/wiki)
 * Looking for community help, browse the already asked [Stack Overflow Questions](http://stackoverflow.com/questions/tagged/support-download-manager) or use the tag: `support-download-manager` when posting a new question


 [1]: http://developer.android.com/reference/android/net/Uri.html
 [2]: https://github.com/novoda/download-manager/tree/master/demo

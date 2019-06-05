# download-manager [![CI status](https://ci.novoda.com/buildStatus/icon?job=download-manager)](https://ci.novoda.com/job/download-manager/lastBuild/console) [![Download from Bintray](https://api.bintray.com/packages/novoda-oss/maven/download-manager/images/download.svg)](https://bintray.com/novoda-oss/maven/download-manager/_latestVersion)[![Apache 2.0 Licence](https://img.shields.io/github/license/novoda/download-manager.svg)](https://github.com/novoda/download-manager/blob/release/LICENSE)

A library that handles long-running downloads, handling the network interactions and retrying downloads automatically after failures. Clients can request
downloads in batches, receiving a single notification for all of the files allocated to a batch while being able to retrieve the single files after downloads complete.

## Adding to your project

To start using this library, add these lines to the `build.gradle` of your project:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.novoda:download-manager:<latest-version>'
}
```

## Simple usage

1. Create a `DownloadManager`:

```java
DownloadManager downloadManager = DownloadManagerBuilder
        .newInstance(this, handler, R.mipmap.ic_launcher_round)
        .withLogHandle(new DemoLogHandle())
        .withStorageRequirementRules(StorageRequirementRuleFactory.createByteBasedRule(TWO_HUNDRED_MB_IN_BYTES))
        .build();
```

2. Create a `Batch` of files to download:

```java
Batch batch = Batch.with(primaryStorageWithDownloadsSubpackage, DownloadBatchIdCreator.createSanitizedFrom("batch_id_1"), "batch one title")
        .downloadFrom("http://ipv4.download.thinkbroadband.com/5MB.zip").saveTo("foo/bar", "local-filename-5mb.zip").withIdentifier(DownloadFileIdCreator.createFrom("file_id_1")).apply()
        .downloadFrom("http://ipv4.download.thinkbroadband.com/10MB.zip").apply()
        .build();
```   

3. Schedule the batch for download:

```java
downloadManager.download(batch);
```

## Snapshots

[![CI status](https://ci.novoda.com/buildStatus/icon?job=download-manager-snapshot)](https://ci.novoda.com/job/download-manager-snapshot/lastBuild/console) [![Download from Bintray](https://api.bintray.com/packages/novoda-oss/snapshots/download-manager/images/download.svg)](https://bintray.com/novoda-oss/snapshots/download-manager/_latestVersion)

Snapshot builds from [`develop`](https://github.com/novoda/download-manager/compare/release...develop) are automatically deployed to a [repository](https://bintray.com/novoda/snapshots/download-manager/_latestVersion) that is not synced with JCenter.
To consume a snapshot build add an additional maven repo as follows:
```groovy
repositories {
    maven {
        url 'https://dl.bintray.com/novoda-oss/snapshots/'
    }
}
```

You can find the latest snapshot version following this [link](https://bintray.com/novoda-oss/snapshots/download-manager/_latestVersion).

## Contributing

We always welcome people to contribute new features or bug fixes, [here is how](https://github.com/novoda/novoda/blob/master/CONTRIBUTING.md).

If you have a problem, check the [Issues Page](https://github.com/novoda/download-manager/issues) first to see if we are already working on it.

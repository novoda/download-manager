package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

class FileDownloaderCreator {

    private final FileDownloaderType type;
    @Nullable
    private final Class<? extends FileDownloader> customClass;

    static FileDownloaderCreator newNetworkFileDownloaderCreator() {
        return new FileDownloaderCreator(FileDownloaderType.NETWORK, null);
    }

    static FileDownloaderCreator newCustomFileDownloaderCreator(Class<? extends FileDownloader> customClass) {
        return new FileDownloaderCreator(FileDownloaderType.CUSTOM, customClass);
    }

    FileDownloaderCreator(FileDownloaderType type, @Nullable Class<? extends FileDownloader> customClass) {
        this.type = type;
        this.customClass = customClass;
    }

    FileDownloader create() {
        return create(type);
    }

    private FileDownloader create(FileDownloaderType type) {
        FileDownloader fileDownloader;

        switch (type) {
            case NETWORK:
                HttpClient httpClient = HttpClientFactory.getInstance();
                NetworkRequestCreator requestCreator = new NetworkRequestCreator();
                fileDownloader = new NetworkFileDownloader(httpClient, requestCreator);
                break;
            case CUSTOM:
                fileDownloader = createCustomFileDownloader();
                break;
            default:
                throw new IllegalStateException("Persistence of type " + type + " is not supported");
        }

        return fileDownloader;
    }

    private FileDownloader createCustomFileDownloader() {
        if (customClass == null) {
            throw new CustomFilePersistenceException("CustomFilePersistence class cannot be accessed, is it public?");
        }

        try {
            ClassLoader systemClassLoader = getClass().getClassLoader();
            Class<?> customFilePersistenceClass = systemClassLoader.loadClass(customClass.getCanonicalName());
            return (FileDownloader) customFilePersistenceClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new CustomFilePersistenceException(customClass, "Class cannot be accessed, is it public?", e);
        } catch (ClassNotFoundException e) {
            throw new CustomFilePersistenceException(customClass, "Class does not exist", e);
        } catch (InstantiationException e) {
            throw new CustomFilePersistenceException(customClass, "Class cannot be instantiated", e);
        }
    }

    private static class CustomFilePersistenceException extends RuntimeException {
        CustomFilePersistenceException(Class customClass, String message, Exception cause) {
            super(customClass.getSimpleName() + ": " + message, cause);
        }

        CustomFilePersistenceException(String message) {
            super(message);
        }
    }
}

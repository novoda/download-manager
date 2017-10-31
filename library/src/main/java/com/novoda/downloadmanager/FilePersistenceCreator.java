package com.novoda.downloadmanager;

import android.content.Context;
import android.support.annotation.Nullable;

class FilePersistenceCreator {

    private final Context context;
    private final FilePersistenceType type;
    @Nullable
    private final Class<? extends FilePersistence> customClass;

    static FilePersistenceCreator newInternalFilePersistenceCreator(Context context) {
        return new FilePersistenceCreator(context, FilePersistenceType.INTERNAL, null);
    }

    static FilePersistenceCreator newExternalFilePersistenceCreator(Context context) {
        return new FilePersistenceCreator(context, FilePersistenceType.EXTERNAL, null);
    }

    static FilePersistenceCreator newCustomFilePersistenceCreator(Context context, Class<? extends FilePersistence> customClass) {
        return new FilePersistenceCreator(context, FilePersistenceType.CUSTOM, customClass);
    }

    FilePersistenceCreator(Context context, FilePersistenceType type, @Nullable Class<? extends FilePersistence> customClass) {
        this.context = context.getApplicationContext();
        this.type = type;
        this.customClass = customClass;
    }

    FilePersistence create() {
        return create(type);
    }

    FilePersistence create(FilePersistenceType type) {
        FilePersistence filePersistence;

        switch (type) {
            case INTERNAL:
                filePersistence = new InternalFilePersistence();
                break;
            case EXTERNAL:
                filePersistence = new ExternalFilePersistence();
                break;
            case CUSTOM:
                filePersistence = createCustomFilePersistence();
                break;
            default:
                throw new IllegalStateException("Persistence of type " + type + " is not supported");
        }

        filePersistence.initialiseWith(context);
        return filePersistence;
    }

    private FilePersistence createCustomFilePersistence() {
        if (customClass == null) {
            throw new CustomFilePersistenceException("CustomFilePersistence class cannot be accessed, is it public?");
        }

        try {
            ClassLoader systemClassLoader = getClass().getClassLoader();
            Class<?> customFilePersistenceClass = systemClassLoader.loadClass(customClass.getCanonicalName());
            return (FilePersistence) customFilePersistenceClass.newInstance();
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

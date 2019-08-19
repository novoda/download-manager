package com.novoda.downloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class LiteBatchFileBuilder implements InternalBatchFileBuilder {

    private static final String SEPARATOR_AS_REGEX = File.separatorChar == '\\' ? "\\\\" : File.separator;

    private final String networkAddress;
    private final DownloadBatchStorageRoot batchStorageRoot;

    private Optional<DownloadFileId> downloadFileId = Optional.absent();
    private Optional<String> path = Optional.absent();
    private Optional<String> fileName = Optional.absent();
    private Optional<FileSize> fileSize = Optional.absent();

    private InternalBatchBuilder parentBuilder;

    LiteBatchFileBuilder(DownloadBatchStorageRoot batchStorageRoot, String networkAddress) {
        this.batchStorageRoot = batchStorageRoot;
        this.networkAddress = networkAddress;
    }

    @Override
    public BatchFileBuilder withParentBuilder(InternalBatchBuilder parentBuilder) {
        this.parentBuilder = parentBuilder;
        return this;
    }

    @Override
    public BatchFileBuilder withIdentifier(DownloadFileId downloadFileId) {
        this.downloadFileId = Optional.fromNullable(downloadFileId);
        return this;
    }

    @Override
    public BatchFileBuilder saveTo(String path) {
        String networkAddressDerivedFileName = FileNameExtractor.extractFrom(networkAddress);
        return saveTo(path, networkAddressDerivedFileName);
    }

    @Override
    public BatchFileBuilder saveTo(String path, String fileName) {
        this.path = Optional.fromNullable(path);
        this.fileName = Optional.fromNullable(fileName);
        return this;
    }

    @Override
    public BatchFileBuilder withSize(FileSize fileSize) {
        this.fileSize = Optional.fromNullable(fileSize);
        return this;
    }

    @Override
    public BatchBuilder apply() {
        String absolutePath = buildPath(
                batchStorageRoot.getBatchStorageRootPath(),
                path.or(""),
                fileName.getOrElse(() -> FileNameExtractor.extractFrom(networkAddress))
        );

        parentBuilder.withFile(new BatchFile(networkAddress, absolutePath, downloadFileId, fileSize));
        return parentBuilder;
    }

    private String buildPath(String... paths) {
        StringBuilder stringBuilder = new StringBuilder();

        String storagePath = paths[0];
        if (storagePath.startsWith(File.separator)) {
            stringBuilder.append(File.separator);
        }

        String joinedPaths = sanitise(joinWithFileSeparator(paths));
        stringBuilder.append(joinedPaths);

        return stringBuilder.toString();
    }

    private String sanitise(String path) {
        String[] pathSegments = path.split(SEPARATOR_AS_REGEX);
        CharSequence[] filteredPathSegments = filterEmptySegmentsOut(pathSegments);
        return joinWithFileSeparator(filteredPathSegments);
    }

    private CharSequence[] filterEmptySegmentsOut(String... pathSegments) {
        List<CharSequence> filteredPathSegments = new ArrayList<>();
        for (String pathSegment : pathSegments) {
            if (!pathSegment.isEmpty()) {
                filteredPathSegments.add(pathSegment);
            }
        }
        return filteredPathSegments.toArray(new CharSequence[filteredPathSegments.size()]);
    }

    private String joinWithFileSeparator(CharSequence... elements) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            stringBuilder.append(elements[i]);
            if (i < elements.length - 1) {
                stringBuilder.append((CharSequence) File.separator);
            }
        }
        return stringBuilder.toString();
    }
}

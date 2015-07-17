package com.novoda.downloadmanager.lib;

class BatchInfo {
    private final String title;
    private final String description;
    private final String bigPictureUrl;
    @NotificationVisibility.Value
    private final int visibility;
    private final String extraData;

    public BatchInfo(String title,
                     String description,
                     String bigPictureUrl,
                     @NotificationVisibility.Value int visibility,
                     String extraData) {
        this.title = title;
        this.description = description;
        this.bigPictureUrl = bigPictureUrl;
        this.visibility = visibility;
        this.extraData = extraData;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBigPictureUrl() {
        return bigPictureUrl;
    }

    @NotificationVisibility.Value
    public int getVisibility() {
        return visibility;
    }

    public String getExtraData() {
        return extraData;
    }

}

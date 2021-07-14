package com.novoda.downloadmanager;

import javax.annotation.Nullable;

public class DownloadError {

    public enum Type {
        FILE_CURRENT_AND_TOTAL_SIZE_MISMATCH,
        FILE_TOTAL_SIZE_REQUEST_FAILED,
        FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE,
        FILE_CANNOT_BE_WRITTEN,
        NETWORK_ERROR_CANNOT_DOWNLOAD_FILE,
        REQUIREMENT_RULE_VIOLATED,
        UNKNOWN
    }

    private final Type type;
    private final String message;
    @Nullable
    private final Integer violatedRuleCode;

    DownloadError(Type type, Integer violatedRuleCode) {
        this(type, "", violatedRuleCode);
    }

    DownloadError(Type type, String message) {
        this(type, message, null);
    }

    private DownloadError(Type type, String message, @Nullable Integer violatedRuleCode) {
        this.type = type;
        this.message = message;
        this.violatedRuleCode = violatedRuleCode;
    }

    public Type type() {
        return type;
    }

    public String message() {
        return message;
    }

    @Nullable
    public Integer violatedRuleCode() {
        return violatedRuleCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadError that = (DownloadError) o;

        if (type != that.type) {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }
        return violatedRuleCode != null ? violatedRuleCode.equals(that.violatedRuleCode) : that.violatedRuleCode == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (violatedRuleCode != null ? violatedRuleCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DownloadError{"
                + "type="
                + type
                + ", message='"
                + message
                + '\''
                + ", violatedRuleCode="
                + violatedRuleCode
                + '}';
    }
}

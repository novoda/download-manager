package com.novoda.downloadmanager.lib;

import java.util.Arrays;

import org.fest.assertions.api.AbstractAssert;
import org.fest.assertions.api.Assertions;

public class CriteriaAssert extends AbstractAssert<CriteriaAssert, Criteria> {

    public CriteriaAssert(Criteria actual) {
        super(actual, CriteriaAssert.class);
    }

    public static CriteriaAssert assertThat(Criteria actual) {
        return new CriteriaAssert(actual);
    }

    public CriteriaAssert hasSelection(String selection) {
        isNotNull();

        Assertions.assertThat(actual.getSelection())
                .overridingErrorMessage("Expected criteria selection to be <%s> but was <%s>", selection, actual.getSelection())
                .isEqualTo(selection);

        return this;
    }

    public CriteriaAssert hasArguments(String[] arguments) {
        isNotNull();

        Assertions.assertThat(actual.getSelectionArguments())
                .overridingErrorMessage("Expected criteria arguments to be <%s> but was <%s>",
                        Arrays.toString(arguments),
                        Arrays.toString(actual.getSelectionArguments()))
                .isEqualTo(arguments);
        return this;
    }

    public CriteriaAssert hasSort(String sort) {
        isNotNull();

        Assertions.assertThat(actual.getSort())
                .overridingErrorMessage("Expected criteria sort to be <%s> but was <%s>", sort, actual.getSort())
                .isEqualTo(sort);

        return this;
    }

}

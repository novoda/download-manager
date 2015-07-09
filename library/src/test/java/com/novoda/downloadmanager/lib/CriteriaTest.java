package com.novoda.downloadmanager.lib;

import java.util.Arrays;

import org.junit.Test;

public class CriteriaTest {

    private static final String SORT_COLUMN = "sort_column";
    private static final String SORT_COLUMN_ASCENDING = "sort_column ASC ";
    private static final String SORT_COLUMN_DESCENDING = "sort_column DESC ";
    private static final String SELECTION_COLUMN = "selection1";
    private static final String SELECTION_QUERY = "selection1=?";
    private static final String ARGUMENT = "arg1";
    private static final String ANOTHER_SELECTION_COLUMN = "selection2";
    private static final String ANOTHER_SELECTION_COLUMN_QUERY = "selection2=?";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String SELECTION_COLUMN_AND_ANOTHER_SELECTION_COLUMN_QUERY = SELECTION_QUERY + AND + ANOTHER_SELECTION_COLUMN_QUERY;
    private static final String SELECTION_COLUMN_OR_ANOTHER_SELECTION_COLUMN_QUERY = SELECTION_QUERY + OR + ANOTHER_SELECTION_COLUMN_QUERY;

    @Test
    public void givenASelectionWhenBuildingThenTheWildcardIsAdded() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .build();

        CriteriaAssert.assertThat(criteria).hasSelection(SELECTION_QUERY);
    }

    @Test
    public void givenASelectionWithArgumentsWhenBuildingThenTheArgumentIsAdded() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSelection(SELECTION_QUERY)
                .hasArguments(new String[]{ARGUMENT});
    }

    @Test
    public void givenACriteriaWithSortAscendingWhenBuildingThenTheSortIsAdded() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .sortBy(SORT_COLUMN)
                .ascending()
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSort(SORT_COLUMN_ASCENDING)
                .hasArguments(new String[]{ARGUMENT});
    }

    @Test
    public void givenACriteriaWithSortDescendingWhenBuildingThenTheSortIsAdded() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .sortBy(SORT_COLUMN)
                .descending()
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSort(SORT_COLUMN_DESCENDING)
                .hasArguments(new String[]{ARGUMENT});
    }

    @Test
    public void givenTwoSelectionsWhenWeBuildWithAndThenTheSelectionQueryIsCorrect() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .and()
                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSelection(SELECTION_COLUMN_AND_ANOTHER_SELECTION_COLUMN_QUERY)
                .hasArguments(new String[]{ARGUMENT, ARGUMENT});
    }

    @Test
    public void givenTwoSelectionsWhenWeBuildWithOrThenTheSelectionQueryIsCorrect() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .or()
                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSelection(SELECTION_COLUMN_OR_ANOTHER_SELECTION_COLUMN_QUERY)
                .hasArguments(new String[]{ARGUMENT, ARGUMENT});
    }

    @Test
    public void givenMultipleSelectionsWhenWeBuildThenTheSelectionQueryIsCorrect() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .or()
                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .and()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSelection(SELECTION_COLUMN_OR_ANOTHER_SELECTION_COLUMN_QUERY + AND + SELECTION_QUERY)
                .hasArguments(new String[]{ARGUMENT, ARGUMENT, ARGUMENT});
    }

    @Test
    public void givenMultipleSelectionsWithInnerCriteriaWhenWeBuildThenTheSelectionQueryIsCorrect() {
        Criteria criteria = new Criteria.Builder()
                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                .withArgument(ARGUMENT)
                .or()
                .withInnerCriteria(
                        new Criteria.Builder()
                                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                .withArgument(ARGUMENT)
                                .and()
                                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                .withArgument(ARGUMENT)
                                .build())
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSelection(SELECTION_QUERY + OR + "(" + ANOTHER_SELECTION_COLUMN_QUERY + AND + SELECTION_QUERY + ")")
                .hasArguments(new String[]{ARGUMENT, ARGUMENT, ARGUMENT});
    }

    @Test
    public void givenMultipleSelectionsWithMultipleInnerCriteriaWhenBuildingThenTheSelectionQueryIsCorrect() {
        Criteria criteria = new Criteria.Builder()
                .withInnerCriteria(
                        new Criteria.Builder()
                                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                .withArgument(ARGUMENT)
                                .or()
                                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                .withArgument(ARGUMENT)
                                .build())
                .and()
                .withInnerCriteria(
                        new Criteria.Builder()
                                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                .withArgument(ARGUMENT)
                                .or()
                                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                .withArgument(ARGUMENT)
                                .or()
                                .withInnerCriteria(
                                        new Criteria.Builder()
                                                .withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                                .withArgument(ARGUMENT)
                                                .and()
                                                .withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS)
                                                .withArgument(ARGUMENT)
                                                .build())
                                .build())
                .sortBy(SORT_COLUMN)
                .ascending()
                .build();

        CriteriaAssert.assertThat(criteria)
                .hasSelection("(" + SELECTION_COLUMN_OR_ANOTHER_SELECTION_COLUMN_QUERY + ")"
                        + AND
                        + "(" + SELECTION_COLUMN_OR_ANOTHER_SELECTION_COLUMN_QUERY
                        + OR
                        + "(" + SELECTION_COLUMN_AND_ANOTHER_SELECTION_COLUMN_QUERY + "))")
                .hasArguments(new String[]{ARGUMENT, ARGUMENT, ARGUMENT, ARGUMENT, ARGUMENT, ARGUMENT});
    }

    @Test
    public void givenMultipleCriteriaWhenApplyingOrThenTheResultingCriteriaIsCorrect() {
        Criteria firstCriteria = new Criteria.Builder().withSelection(SELECTION_COLUMN, Criteria.Wildcard.EQUALS).withArgument(ARGUMENT).build();
        Criteria secondCriteria = new Criteria.Builder().withSelection(ANOTHER_SELECTION_COLUMN, Criteria.Wildcard.EQUALS).withArgument(ARGUMENT).build();

        Criteria orCriteria = new Criteria.Builder().joinWithOr(Arrays.asList(firstCriteria, secondCriteria)).build();

        CriteriaAssert.assertThat(orCriteria)
                .hasSelection(SELECTION_QUERY + OR + ANOTHER_SELECTION_COLUMN_QUERY)
                .hasArguments(new String[]{ARGUMENT, ARGUMENT});
    }
}

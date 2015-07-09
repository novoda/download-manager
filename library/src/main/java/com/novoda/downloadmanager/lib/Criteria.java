package com.novoda.downloadmanager.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Criteria {

    private final String selection;
    private final String sort;
    private final String[] selectionArguments;

    Criteria(String selection, String[] selectionArguments, String sort) {
        this.selection = selection;
        this.sort = sort;
        this.selectionArguments = selectionArguments;
    }

    public String getSelection() {
        return selection;
    }

    public String[] getSelectionArguments() {
        return Arrays.copyOf(selectionArguments, selectionArguments.length);
    }

    public String getSort() {
        return sort;
    }

    public static class Builder {
        private static final String SPACE = " ";
        private static final String AND_BETWEEN_SELECTIONS = "AND";
        private static final String OR_BETWEEN_SELECTIONS = "OR";
        private static final String OPEN_BRACKET = "(";
        private static final String CLOSE_BRACKET = ")";

        private final ArgumentBuilder argumentBuilder;
        private final SortBuilder sortBuilder;
        private final SelectionBuilder selectionBuilder;

        public Builder() {
            argumentBuilder = new ArgumentBuilder(this);
            sortBuilder = new SortBuilder(this);
            selectionBuilder = new SelectionBuilder(this);
        }

        public ArgumentBuilder withSelection(String selection, Wildcard wildcard) {
            selectionBuilder.withSelection(selection).withWildcard(wildcard);
            return argumentBuilder;
        }

        public Builder withInnerCriteria(Criteria criteria) {
            selectionBuilder.withSelection(OPEN_BRACKET + criteria.getSelection() + CLOSE_BRACKET);
            argumentBuilder.withArguments(criteria.getSelectionArguments());
            return this;
        }

        public Builder and() {
            selectionBuilder.withSelection(SPACE + AND_BETWEEN_SELECTIONS + SPACE);
            return this;
        }

        public Builder or() {
            selectionBuilder.withSelection(SPACE + OR_BETWEEN_SELECTIONS + SPACE);
            return this;
        }

        public Builder joinWithOr(List<Criteria> criteriaList) {
            for (Criteria criteria : criteriaList) {
                selectionBuilder.withSelection(criteria.getSelection());
                argumentBuilder.withArguments(criteria.getSelectionArguments());
                if (isNotLastIn(criteriaList, criteria)) {
                    or();
                }
            }
            return this;
        }

        private boolean isNotLastIn(List<Criteria> criteriaList, Criteria criteria) {
            return criteriaList.indexOf(criteria) != criteriaList.size() - 1;
        }

        public SortBuilder sortBy(String sortColumn) {
            return sortBuilder.sortBy(sortColumn);

        }

        public Criteria build() {
            return new Criteria(selectionBuilder.build(), argumentBuilder.build(), sortBuilder.build());
        }

        public static class SelectionBuilder {
            private final Builder builder;

            private String selection = "";

            public SelectionBuilder(Builder builder) {
                this.builder = builder;
            }

            private SelectionBuilder withSelection(String selection) {
                this.selection += selection;
                return this;
            }

            private Builder withWildcard(Wildcard wildcard) {
                this.selection += wildcard.toSql();
                return builder;
            }

            String build() {
                return selection;
            }
        }

        public static class ArgumentBuilder {
            private final Builder builder;
            private final List<String> selectionArguments;

            ArgumentBuilder(Builder builder) {
                this.builder = builder;
                this.selectionArguments = new ArrayList<>();
            }

            public Builder withArgument(String argument) {
                selectionArguments.add(argument);
                return builder;
            }

            private Builder withArguments(String[] arguments) {
                selectionArguments.addAll(Arrays.asList(arguments));
                return builder;
            }

            String[] build() {
                return selectionArguments.toArray(new String[selectionArguments.size()]);
            }
        }

        public static class SortBuilder {

            private static final String ASCENDING_SORT = "ASC";
            private static final String DESCENDING_SORT = "DESC";

            private final Builder builder;
            private String sort = "";

            public SortBuilder(Builder builder) {
                this.builder = builder;
            }

            public Builder ascending() {
                this.sort += (SPACE + ASCENDING_SORT + SPACE);
                return builder;
            }

            public Builder descending() {
                this.sort += (SPACE + DESCENDING_SORT + SPACE);
                return builder;
            }

            private SortBuilder sortBy(String sort) {
                this.sort += sort;
                return this;
            }

            String build() {
                return sort;
            }
        }
    }

    public enum Wildcard {
        EQUALS("=?"),
        LESS_THAN("<?"),
        MORE_THAN(">?"),
        MORE_THAN_EQUAL(">=?");

        private final String sqlValue;

        Wildcard(String wildcard) {
            this.sqlValue = wildcard;
        }

        String toSql() {
            return sqlValue;
        }
    }
}

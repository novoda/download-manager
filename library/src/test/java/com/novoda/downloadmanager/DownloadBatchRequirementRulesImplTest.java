package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.DownloadBatchRequirementRulesImplFixtures.withRules;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class DownloadBatchRequirementRulesImplTest {
    DownloadBatchStatus status = mock(DownloadBatchStatus.class);

    @Test
    public void noViolatedRuleIsIdentified_whenAllRulesAreSatisfied() {
        DownloadBatchRequirementRule rule1Satisfied = mock(DownloadBatchRequirementRule.class);
        DownloadBatchRequirementRule rule2Satisfied = mock(DownloadBatchRequirementRule.class);

        given(rule1Satisfied.getCode()).willReturn(1);
        given(rule1Satisfied.hasViolatedRule(status)).willReturn(false);

        given(rule2Satisfied.getCode()).willReturn(2);
        given(rule2Satisfied.hasViolatedRule(status)).willReturn(false);

        DownloadBatchRequirementRulesImpl rules = withRules(rule1Satisfied, rule2Satisfied);

        assertThat(rules.hasViolatedRule(status)).isFalse();
        Optional<DownloadBatchRequirementRule> violatedRule = rules.getViolatedRule(status);
        assertThat(violatedRule.isAbsent()).isTrue();
    }

    @Test
    public void ruleIsCorrectlyIdentified_whenRuleIsViolated() {
        DownloadBatchRequirementRule rule1Satisfied = mock(DownloadBatchRequirementRule.class);
        DownloadBatchRequirementRule rule2Violated = mock(DownloadBatchRequirementRule.class);

        given(rule1Satisfied.getCode()).willReturn(1);
        given(rule1Satisfied.hasViolatedRule(status)).willReturn(false);

        given(rule2Violated.getCode()).willReturn(2);
        given(rule2Violated.hasViolatedRule(status)).willReturn(true);

        DownloadBatchRequirementRulesImpl rules = withRules(rule1Satisfied, rule2Violated);

        assertThat(rules.hasViolatedRule(status)).isTrue();
        Optional<DownloadBatchRequirementRule> violatedRule = rules.getViolatedRule(status);
        assertThat(violatedRule.isPresent()).isTrue();
        assertThat(violatedRule.get().getCode()).isEqualTo(2);
    }
}
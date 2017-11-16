package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;

public class FixedRateTimerActionSchedulerTest {

    private static final long FREQUENCY = 100;

    private final Timer timer = mock(Timer.class);
    private final Map<ActionScheduler.Action, TimerTask> actionTimerTasks = new HashMap<>();
    private final ActionScheduler.Action anyAction = mock(ActionScheduler.Action.class);
    private TimerTask timerTask;

    private FixedRateTimerActionScheduler scheduler;

    @Before
    public void setUp() {
        scheduler = new FixedRateTimerActionScheduler(timer, FREQUENCY, actionTimerTasks);

        final ArgumentCaptor<TimerTask> argumentCaptor = ArgumentCaptor.forClass(TimerTask.class);
        willAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                timerTask = argumentCaptor.getValue();
                return null;
            }
        }).given(timer).scheduleAtFixedRate(argumentCaptor.capture(), eq(0L), eq(FREQUENCY));
    }

    @Test
    public void executesAction_whenSchedulingAction() {
        scheduler.schedule(anyAction);

        timerTask.run();

        verify(anyAction).perform();
    }

    @Test
    public void recordsAction_whenSchedulingAction() {
        scheduler.schedule(anyAction);

        assertThat(actionTimerTasks).containsEntry(anyAction, timerTask);
    }

    @Test
    public void doesNothing_whenActionAlreadyScheduled() {
        givenScheduledAction();

        scheduler.schedule(anyAction);

        verifyZeroInteractions(timer);
    }

    @Test
    public void cancelTimerTask_whenCancellingSpecificAction() {
        TimerTask timerTask = mock(TimerTask.class);
        actionTimerTasks.put(anyAction, timerTask);

        scheduler.cancel(anyAction);

        verify(timerTask).cancel();
    }

    @Test
    public void removeTimerTask_whenCancellingSpecificAction() {
        TimerTask timerTask = mock(TimerTask.class);
        actionTimerTasks.put(anyAction, timerTask);

        ActionScheduler.Action additionalAction = mock(ActionScheduler.Action.class);
        TimerTask additionalTimerTask = mock(TimerTask.class);
        actionTimerTasks.put(anyAction, timerTask);
        actionTimerTasks.put(additionalAction, additionalTimerTask);

        scheduler.cancel(anyAction);

        assertThat(actionTimerTasks).doesNotContainEntry(anyAction, timerTask);
        assertThat(actionTimerTasks).containsEntry(additionalAction, additionalTimerTask);
    }

    @Test
    public void clearsTimerTasks_whenCancellingAll() {
        givenScheduledAction();

        scheduler.cancelAll();

        verify(timer).cancel();
    }

    @Test
    public void clearsAllActions_whenCancellingAll() {
        givenScheduledAction();

        scheduler.cancelAll();

        assertThat(actionTimerTasks).isEmpty();
    }

    @Test
    public void returnsTrue_whenActionIsScheduled() {
        givenScheduledAction();

        boolean scheduled = scheduler.isScheduled(anyAction);

        assertThat(scheduled).isTrue();
    }

    @Test
    public void returnsFalse_whenActionIsNotScheduled() {
        boolean scheduled = scheduler.isScheduled(anyAction);

        assertThat(scheduled).isFalse();
    }

    private void givenScheduledAction() {
        scheduler.schedule(anyAction);
        reset(timer);
    }

}

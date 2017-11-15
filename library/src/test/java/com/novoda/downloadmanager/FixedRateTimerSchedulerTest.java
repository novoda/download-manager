package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

public class FixedRateTimerSchedulerTest {

    private static final long FREQUENCY = 100;

    private final Timer timer = mock(Timer.class);
    private final List<Scheduler.Action> actions = new ArrayList<>();
    private final Scheduler.Action anyAction = mock(Scheduler.Action.class);
    private final FixedRateTimerScheduler scheduler = new FixedRateTimerScheduler(timer, FREQUENCY, actions);

    @Test
    public void executesAction_whenSchedulingAction() {
        scheduler.schedule(anyAction);

        ArgumentCaptor<TimerTask> argumentCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timer).scheduleAtFixedRate(argumentCaptor.capture(), eq(0L), eq(FREQUENCY));
        argumentCaptor.getValue().run();

        verify(anyAction).perform();
    }

    @Test
    public void doesNothing_whenActionAlreadyScheduled() {
        givenScheduledAction();

        scheduler.schedule(anyAction);

        verifyZeroInteractions(timer);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwsException_whenCancellingSpecificAction() {
        givenScheduledAction();

        scheduler.cancel(anyAction);

        verify(timer).cancel();
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

        assertThat(actions).isEmpty();
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

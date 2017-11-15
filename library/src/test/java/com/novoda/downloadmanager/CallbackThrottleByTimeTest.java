package com.novoda.downloadmanager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.*;

public class CallbackThrottleByTimeTest {

    private final Scheduler scheduler = mock(Scheduler.class);
    private final DownloadBatchCallback callback = mock(DownloadBatchCallback.class);
    private final DownloadBatchStatus downloadBatchStatus = mock(DownloadBatchStatus.class);

    private CallbackThrottleByTime callbackThrottleByTime;

    @Before
    public void setUp() {
        callbackThrottleByTime = new CallbackThrottleByTime(scheduler);

        final ArgumentCaptor<Scheduler.Action> argumentCaptor = ArgumentCaptor.forClass(Scheduler.Action.class);
        willAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                argumentCaptor.getValue().perform();
                return null;
            }
        }).given(scheduler).schedule(argumentCaptor.capture());
    }

    @Test
    public void doesNothing_whenCallbackIsAbsent() {
        callbackThrottleByTime.update(downloadBatchStatus);

        verifyZeroInteractions(scheduler, callback, downloadBatchStatus);
    }

    @Test
    public void doesNotSchedule_whenActionAlreadyScheduled() {
        given(scheduler.isScheduled(any(Scheduler.Action.class))).willReturn(true);
        callbackThrottleByTime.setCallback(callback);

        callbackThrottleByTime.update(downloadBatchStatus);

        verify(scheduler, never()).schedule(any(Scheduler.Action.class));
    }

    @Test
    public void emitsStatus_whenSchedulingUniqueAction() {
        callbackThrottleByTime.setCallback(callback);

        callbackThrottleByTime.update(downloadBatchStatus);

        verify(callback).onUpdate(downloadBatchStatus);
    }

    @Test
    public void cancelsAllScheduledActions_whenStoppingUpdates() {
        callbackThrottleByTime.stopUpdates();

        verify(scheduler).cancelAll();
    }

    @Test
    public void emitsLastStatus_whenStoppingUpdates() {
        callbackThrottleByTime.setCallback(callback);

        callbackThrottleByTime.stopUpdates();

        verify(scheduler).cancelAll();
    }

    @Test
    public void doesNotEmitLastStatus_whenCallbackAbsent() {
        callbackThrottleByTime.stopUpdates();

        verifyZeroInteractions(callback);
    }

}

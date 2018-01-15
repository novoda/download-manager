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

    private final ActionScheduler actionScheduler = mock(ActionScheduler.class);
    private final DownloadBatchStatusCallback callback = mock(DownloadBatchStatusCallback.class);
    private final DownloadBatchStatus downloadBatchStatus = mock(DownloadBatchStatus.class);

    private CallbackThrottleByTime callbackThrottleByTime;

    @Before
    public void setUp() {
        callbackThrottleByTime = new CallbackThrottleByTime(actionScheduler);

        final ArgumentCaptor<ActionScheduler.Action> argumentCaptor = ArgumentCaptor.forClass(ActionScheduler.Action.class);
        willAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                argumentCaptor.getValue().perform();
                return null;
            }
        }).given(actionScheduler).schedule(argumentCaptor.capture());
    }

    @Test
    public void doesNothing_whenCallbackIsAbsent() {
        callbackThrottleByTime.update(downloadBatchStatus);

        verifyZeroInteractions(actionScheduler, callback, downloadBatchStatus);
    }

    @Test
    public void doesNotSchedule_whenActionAlreadyScheduled() {
        given(actionScheduler.isScheduled(any(ActionScheduler.Action.class))).willReturn(true);
        callbackThrottleByTime.setCallback(callback);

        callbackThrottleByTime.update(downloadBatchStatus);

        verify(actionScheduler, never()).schedule(any(ActionScheduler.Action.class));
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

        verify(actionScheduler).cancelAll();
    }

    @Test
    public void emitsLastStatus_whenStoppingUpdates() {
        callbackThrottleByTime.setCallback(callback);

        callbackThrottleByTime.stopUpdates();

        verify(actionScheduler).cancelAll();
    }

    @Test
    public void doesNotEmitLastStatus_whenCallbackAbsent() {
        callbackThrottleByTime.stopUpdates();

        verifyZeroInteractions(callback);
    }

}

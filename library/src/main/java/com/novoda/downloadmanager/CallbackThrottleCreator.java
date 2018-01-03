package com.novoda.downloadmanager;

import java.util.concurrent.TimeUnit;

final class CallbackThrottleCreator {

    private static final Class<? extends CallbackThrottle> NO_CUSTOM_CALLBACK_THROTTLE = null;
    private static final TimeUnit UNUSED_TIME_UNIT = TimeUnit.SECONDS;
    private static final int UNUSED_FREQUENCY = 0;

    enum Type {
        THROTTLE_BY_TIME,
        THROTTLE_BY_PROGRESS_INCREASE,
        CUSTOM
    }

    private final Type type;
    private final TimeUnit timeUnit;
    private final long frequency;
    private final Class<? extends CallbackThrottle> customCallbackThrottle;

    static CallbackThrottleCreator byTime(TimeUnit timeUnit, long quantity) {
        return new CallbackThrottleCreator(Type.THROTTLE_BY_TIME, timeUnit, quantity, NO_CUSTOM_CALLBACK_THROTTLE);
    }

    static CallbackThrottleCreator byProgressIncrease() {
        return new CallbackThrottleCreator(Type.THROTTLE_BY_PROGRESS_INCREASE, UNUSED_TIME_UNIT, UNUSED_FREQUENCY, NO_CUSTOM_CALLBACK_THROTTLE);
    }

    static CallbackThrottleCreator byCustomThrottle(Class<? extends CallbackThrottle> customCallbackThrottle) {
        return new CallbackThrottleCreator(Type.CUSTOM, UNUSED_TIME_UNIT, UNUSED_FREQUENCY, customCallbackThrottle);
    }

    private CallbackThrottleCreator(Type type, TimeUnit timeUnit, long frequency, Class<? extends CallbackThrottle> customCallbackThrottle) {
        this.type = type;
        this.timeUnit = timeUnit;
        this.frequency = frequency;
        this.customCallbackThrottle = customCallbackThrottle;
    }

    CallbackThrottle create() {
        switch (type) {
            case THROTTLE_BY_TIME:
                ActionScheduler actionScheduler = SchedulerFactory.createFixedRateTimerScheduler(timeUnit.toMillis(frequency));
                return new CallbackThrottleByTime(actionScheduler);
            case THROTTLE_BY_PROGRESS_INCREASE:
                return new CallbackThrottleByProgressIncrease();
            case CUSTOM:
                return createCallbackThrottle();
            default:
                throw new IllegalStateException("type " + type + " not supported.");
        }
    }

    private CallbackThrottle createCallbackThrottle() {
        if (customCallbackThrottle == null) {
            throw new CustomCallbackThrottleException("CustomCallbackThrottle class cannot be accessed, is it public?");
        }

        try {
            ClassLoader systemClassLoader = getClass().getClassLoader();
            Class<?> customCallbackThrottleClass = systemClassLoader.loadClass(customCallbackThrottle.getCanonicalName());
            return (CallbackThrottle) customCallbackThrottleClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new CustomCallbackThrottleException(customCallbackThrottle, "Class cannot be accessed, is it public?", e);
        } catch (ClassNotFoundException e) {
            throw new CustomCallbackThrottleException(customCallbackThrottle, "Class does not exist", e);
        } catch (InstantiationException e) {
            throw new CustomCallbackThrottleException(customCallbackThrottle, "Class cannot be instantiated", e);
        }
    }

    private static class CustomCallbackThrottleException extends RuntimeException {
        CustomCallbackThrottleException(Class customClass, String message, Exception cause) {
            super(customClass.getSimpleName() + ": " + message, cause);
        }

        CustomCallbackThrottleException(String message) {
            super(message);
        }
    }
}

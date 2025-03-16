package com.bicycle.util;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadFactory;

@RequiredArgsConstructor
public class NamedThreadFactory implements ThreadFactory {

    private int threadIndex;
    private final String threadNamePrefix;
    private final boolean isDaemon;

    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = new Thread(runnable, threadNamePrefix + ++threadIndex);
        thread.setDaemon(isDaemon);
        return thread;
    }
}

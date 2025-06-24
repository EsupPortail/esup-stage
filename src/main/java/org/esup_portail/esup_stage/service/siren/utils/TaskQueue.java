package org.esup_portail.esup_stage.service.siren.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {
    private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public static void submit(Runnable task) {
        queue.add(task);
    }

    public static Runnable take() throws InterruptedException {
        return queue.take();
    }
}

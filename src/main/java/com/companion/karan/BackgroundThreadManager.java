package com.companion.karan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundThreadManager {
    private static BackgroundThreadManager instance;
    private ExecutorService executorService;

    private BackgroundThreadManager() {
        executorService = Executors.newCachedThreadPool();
    }

    public static synchronized BackgroundThreadManager getInstance() {
        if (instance == null) {
            instance = new BackgroundThreadManager();
        }
        return instance;
    }

    public void execute(Runnable task) {
        executorService.submit(task);
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

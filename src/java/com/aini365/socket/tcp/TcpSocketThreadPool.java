package com.aini365.socket.tcp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpSocketThreadPool {

    private static ExecutorService service= Executors.newCachedThreadPool();

    public static void submit(Runnable runnable){
        service.submit(runnable);
    }

}

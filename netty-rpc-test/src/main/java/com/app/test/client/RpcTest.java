package com.app.test.client;

import com.app.test.service.HelloService;
import com.netty.rpc.client.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcTest {

    public static void main(String[] args) throws InterruptedException {
        final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
        int threadNum = 5;
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 5, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));

        try {
            for (int i = 0; i < threadNum; ++i) {
                threadPool.execute(() -> {
                    try {
                        final HelloService syncClient = RpcClient.createService(HelloService.class, "1.0");
                        String result = syncClient.hello("" + Thread.currentThread().getName());
                        log.info(result);
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            // 阻塞住主线程，直到线程池内的所有线程被执行完毕
            threadPool.awaitTermination(60L, TimeUnit.SECONDS);
            rpcClient.stop();
        }
    }
}
